package com.codeiy.auth.oauth.endpoint;

import cn.hutool.core.util.StrUtil;
import com.codeiy.core.util.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.*;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenRequest;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestValidator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;

/**
 * OAuth授权自定义端点
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class AuthEndpoint {
    private final ClientDetailsService clientDetailsService;
    private final ImplicitTokenGranter implicitTokenGranter;
    private final AuthorizationCodeServices authorizationCodeServices;
    private final OAuth2RequestFactory oAuth2RequestFactory;

    private RedirectResolver redirectResolver = new DefaultRedirectResolver();
    private OAuth2RequestValidator oauth2RequestValidator = new DefaultOAuth2RequestValidator();


    /**
     * 授权确认页面
     *
     * @return
     */
    @GetMapping("/confirmAccess")
    public R confirmAccess(HttpServletRequest request) {
        String clientId = request.getParameter("clientId");
        if (StrUtil.isBlank(clientId)) {
            return R.failed("clientId不能为空");
        }
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
        if (clientDetails == null) {
            return R.failed("根据clientId查无数据");
        }
        return R.ok(clientDetails);
    }

    /**
     * 自定义确认授权
     *
     * @return
     */
    @RequestMapping("/customAuthorize")
    public R customAuthorize(HttpServletRequest request, @RequestParam Map<String, String> parameters) {
        AuthorizationRequest authorizationRequest = oAuth2RequestFactory.createAuthorizationRequest(parameters);
        Set<String> responseTypes = authorizationRequest.getResponseTypes();
        if (!responseTypes.contains("token") && !responseTypes.contains("code")) {
            throw new UnsupportedResponseTypeException("Unsupported response types: " + responseTypes);
        }
        if (authorizationRequest.getClientId() == null) {
            throw new InvalidClientException("A client id must be provided");
        }

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal == null || !principal.isAuthenticated()) {
            throw new InsufficientAuthenticationException(
                    "User must be authenticated with Spring Security before authorization can be completed.");
        }

        ClientDetails client = clientDetailsService.loadClientByClientId(authorizationRequest.getClientId());

        // The resolved redirect URI is either the redirect_uri from the parameters or the one from
        // clientDetails. Either way we need to store it on the AuthorizationRequest.
        String redirectUriParameter = authorizationRequest.getRequestParameters().get(OAuth2Utils.REDIRECT_URI);
        String resolvedRedirect = redirectResolver.resolveRedirect(redirectUriParameter, client);
        if (!StringUtils.hasText(resolvedRedirect)) {
            throw new RedirectMismatchException(
                    "A redirectUri must be either supplied or preconfigured in the ClientDetails");
        }
        authorizationRequest.setRedirectUri(resolvedRedirect);

        // We intentionally only validate the parameters requested by the client (ignoring any data that may have
        // been added to the request by the manager).
        oauth2RequestValidator.validateScope(authorizationRequest, client);

        authorizationRequest.setApproved("true".equals(parameters.get("user_oauth_approval")));

        // Validation is all done, so we can check for auto approval...
        if (authorizationRequest.isApproved()) {
            if (responseTypes.contains("token")) {
                ModelAndView mv = getImplicitGrantResponse(authorizationRequest);
            }
            if (responseTypes.contains("code")) {
                String url = getSuccessfulRedirect(authorizationRequest, generateCode(authorizationRequest, principal));
                log.info(url);
            }
        }


        String clientId = request.getParameter("clientId");
        if (StrUtil.isBlank(clientId)) {
            return R.failed("clientId不能为空");
        }
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
        if (clientDetails == null) {
            return R.failed("根据clientId查无数据");
        }
        return R.ok(clientDetails);
    }


    private String getSuccessfulRedirect(AuthorizationRequest authorizationRequest, String authorizationCode) {

        if (authorizationCode == null) {
            throw new IllegalStateException("No authorization code found in the current request scope.");
        }

        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("code", authorizationCode);

        String state = authorizationRequest.getState();
        if (state != null) {
            query.put("state", state);
        }

        return append(authorizationRequest.getRedirectUri(), query, null, false);
    }


    private String generateCode(AuthorizationRequest authorizationRequest, Authentication authentication)
            throws AuthenticationException {

        try {

            OAuth2Request storedOAuth2Request = oAuth2RequestFactory.createOAuth2Request(authorizationRequest);

            OAuth2Authentication combinedAuth = new OAuth2Authentication(storedOAuth2Request, authentication);
            String code = authorizationCodeServices.createAuthorizationCode(combinedAuth);

            return code;

        } catch (OAuth2Exception e) {

            if (authorizationRequest.getState() != null) {
                e.addAdditionalInformation("state", authorizationRequest.getState());
            }

            throw e;

        }
    }

    private ModelAndView getImplicitGrantResponse(AuthorizationRequest authorizationRequest) {
        try {
            TokenRequest tokenRequest = oAuth2RequestFactory.createTokenRequest(authorizationRequest, "implicit");
            OAuth2Request storedOAuth2Request = oAuth2RequestFactory.createOAuth2Request(authorizationRequest);
            OAuth2AccessToken accessToken = getAccessTokenForImplicitGrant(tokenRequest, storedOAuth2Request);
            if (accessToken == null) {
                throw new UnsupportedResponseTypeException("Unsupported response type: token");
            }
            return new ModelAndView(new RedirectView(appendAccessToken(authorizationRequest, accessToken), false, true,
                    false));
        } catch (OAuth2Exception e) {
            return new ModelAndView(new RedirectView(getUnsuccessfulRedirect(authorizationRequest, e, true), false,
                    true, false));
        }
    }

    private String appendAccessToken(AuthorizationRequest authorizationRequest, OAuth2AccessToken accessToken) {

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        Map<String, String> keys = new HashMap<String, String>();

        if (accessToken == null) {
            throw new InvalidRequestException("An implicit grant could not be made");
        }

        vars.put("access_token", accessToken.getValue());
        vars.put("token_type", accessToken.getTokenType());
        String state = authorizationRequest.getState();

        if (state != null) {
            vars.put("state", state);
        }
        Date expiration = accessToken.getExpiration();
        if (expiration != null) {
            long expires_in = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            vars.put("expires_in", expires_in);
        }
        String originalScope = authorizationRequest.getRequestParameters().get(OAuth2Utils.SCOPE);
        if (originalScope == null || !OAuth2Utils.parseParameterList(originalScope).equals(accessToken.getScope())) {
            vars.put("scope", OAuth2Utils.formatParameterList(accessToken.getScope()));
        }
        Map<String, Object> additionalInformation = accessToken.getAdditionalInformation();
        for (String key : additionalInformation.keySet()) {
            Object value = additionalInformation.get(key);
            if (value != null) {
                keys.put("extra_" + key, key);
                vars.put("extra_" + key, value);
            }
        }
        // Do not include the refresh token (even if there is one)
        return append(authorizationRequest.getRedirectUri(), vars, keys, true);
    }

    private String getUnsuccessfulRedirect(AuthorizationRequest authorizationRequest, OAuth2Exception failure,
                                           boolean fragment) {

        if (authorizationRequest == null || authorizationRequest.getRedirectUri() == null) {
            // we have no redirect for the user. very sad.
            throw new UnapprovedClientAuthenticationException("Authorization failure, and no redirect URI.", failure);
        }

        Map<String, String> query = new LinkedHashMap<String, String>();

        query.put("error", failure.getOAuth2ErrorCode());
        query.put("error_description", failure.getMessage());

        if (authorizationRequest.getState() != null) {
            query.put("state", authorizationRequest.getState());
        }

        if (failure.getAdditionalInformation() != null) {
            for (Map.Entry<String, String> additionalInfo : failure.getAdditionalInformation().entrySet()) {
                query.put(additionalInfo.getKey(), additionalInfo.getValue());
            }
        }

        return append(authorizationRequest.getRedirectUri(), null, query, fragment);

    }

    private String append(String base, Map<String, ?> query, Map<String, String> keys, boolean fragment) {

        UriComponentsBuilder template = UriComponentsBuilder.newInstance();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(base);
        URI redirectUri;
        try {
            // assume it's encoded to start with (if it came in over the wire)
            redirectUri = builder.build(true).toUri();
        } catch (Exception e) {
            // ... but allow client registrations to contain hard-coded non-encoded values
            redirectUri = builder.build().toUri();
            builder = UriComponentsBuilder.fromUri(redirectUri);
        }
        template.scheme(redirectUri.getScheme()).port(redirectUri.getPort()).host(redirectUri.getHost())
                .userInfo(redirectUri.getUserInfo()).path(redirectUri.getPath());

        if (fragment) {
            StringBuilder values = new StringBuilder();
            if (redirectUri.getFragment() != null) {
                String append = redirectUri.getFragment();
                values.append(append);
            }
            for (String key : query.keySet()) {
                if (values.length() > 0) {
                    values.append("&");
                }
                String name = key;
                if (keys != null && keys.containsKey(key)) {
                    name = keys.get(key);
                }
                values.append(name + "={" + key + "}");
            }
            if (values.length() > 0) {
                template.fragment(values.toString());
            }
            UriComponents encoded = template.build().expand(query).encode();
            builder.fragment(encoded.getFragment());
        } else {
            for (String key : query.keySet()) {
                String name = key;
                if (keys != null && keys.containsKey(key)) {
                    name = keys.get(key);
                }
                template.queryParam(name, "{" + key + "}");
            }
            template.fragment(redirectUri.getFragment());
            UriComponents encoded = template.build().expand(query).encode();
            builder.query(encoded.getQuery());
        }

        return builder.build().toUriString();

    }

    private synchronized OAuth2AccessToken getAccessTokenForImplicitGrant(TokenRequest tokenRequest,
                                                                          OAuth2Request storedOAuth2Request) {
        return implicitTokenGranter.grant("implicit", new ImplicitTokenRequest(tokenRequest, storedOAuth2Request));
    }
}
