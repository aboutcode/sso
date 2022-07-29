package com.codeiy.auth.oauth.service;

import cn.hutool.core.util.StrUtil;
import com.codeiy.core.constant.AuthConstants;
import com.codeiy.core.util.R;
import com.codeiy.user.client.AppClient;
import com.codeiy.user.dto.AppDTO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
public class ClientDetailsServiceImpl implements ClientDetailsService {
    @Setter
    private AppClient appClient;
    @Setter
    private PasswordEncoder passwordEncoder;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        R<AppDTO> restResponse = appClient.getByClientId(clientId);
        if (restResponse == null || restResponse.getData() == null) {
            log.error("找不到clientId: " + clientId);
            return null;
        }
        AppDTO app = restResponse.getData();
        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setClientId(clientId);
        clientDetails.setClientSecret(passwordEncoder.encode(app.getClientSecret()));
        String authorizedGrantTypes = app.getAuthorizedGrantTypes();
        if (StrUtil.isBlank(authorizedGrantTypes)) {
            authorizedGrantTypes = "authorization_code,implicit,refresh_token";
        }
        clientDetails.setAuthorizedGrantTypes(Arrays.asList(authorizedGrantTypes.split(",")));
        clientDetails.setRegisteredRedirectUri(Collections.singleton(app.getRedirectUrl()));
        String scopes = app.getScopes();
        if (StrUtil.isBlank(scopes)) {
            scopes = AuthConstants.SCOPE_OPENID + "," + AuthConstants.SCOPE_PROFILE;
        }
        clientDetails.setScope(Arrays.asList(scopes.split(",")));
        if (AppDTO.AUTO_APPROVE_YES.equals(app.getAutoApprove())) {
            clientDetails.setAutoApproveScopes(Arrays.asList(scopes.split(",")));
        }
//        clientDetails.setAdditionalInformation(additionalInformation);
//        clientDetails.setAuthorities(AuthorityUtils.createAuthorityList(authorities.toArray(new String[authorities.size()])));
//        clientDetails.setResourceIds(resourceIds);
        clientDetails.setAccessTokenValiditySeconds(7200);
        clientDetails.setRefreshTokenValiditySeconds(259200);
        return clientDetails;
    }
}
