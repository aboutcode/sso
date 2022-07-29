package com.codeiy.auth.oauth.config;

import com.codeiy.auth.oauth.service.AuthenticationCodeServiceImpl;
import com.codeiy.auth.oauth.service.ClientDetailsServiceImpl;
import com.codeiy.core.constant.AuthConstants;
import com.codeiy.user.client.AppClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * 通过注解{@code @EnableAuthorizationServer}声明认证服务器
 * 基于{@link ClientDetailsServiceImpl}自定义应用系统信息管理
 * 基于Redis存储OAuth协议的AccessToken
 * 声明OAuth2.0简化模式AccessToken生成规则
 * 自定义Auth2.0协议确认授权以及认证请求链接
 */
@RequiredArgsConstructor
@EnableAuthorizationServer
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    private final AppClient appClient;
    private final AuthenticationManager authenticationManager;
    private final RedisConnectionFactory redisConnectionFactory;
    private final PasswordEncoder passwordEncoder;

    private ClientDetailsServiceImpl clientDetailsService;
    private RedisTokenStore redisTokenStore;
    private AuthorizationServerTokenServices tokenServices;
    private OAuth2RequestFactory oAuth2RequestFactory;
    private ImplicitTokenGranter implicitTokenGranter;
    private AuthorizationCodeServices authorizationCodeServices;

    /**
     * 基于Redis存储OAuth协议的AccessToken
     */
    @Bean
    public TokenStore tokenStore() {
        if (redisTokenStore == null) {
            redisTokenStore = new RedisTokenStore(redisConnectionFactory);
            redisTokenStore.setPrefix(AuthConstants.OAUTH_PREFIX);
        }
        return redisTokenStore;
    }

    /**
     * 令牌权限范围配置，使用默认的{@link DefaultOAuth2RequestFactory},可匹配到用户的角色。
     */
    @Bean
    public OAuth2RequestFactory oAuth2RequestFactory() {
        if (oAuth2RequestFactory == null) {
            oAuth2RequestFactory = new DefaultOAuth2RequestFactory(clientDetailsService());
        }
        return oAuth2RequestFactory;
    }

    /**
     * OAuth2.0简化模式AccessToken生成规则
     */
    @Bean
    public ImplicitTokenGranter implicitTokenGranter() {
        if (implicitTokenGranter == null) {
            implicitTokenGranter = new ImplicitTokenGranter(tokenServices(), clientDetailsService(), oAuth2RequestFactory());
        }
        return implicitTokenGranter;
    }

    /**
     * OAuth2.0授权码模式，自定义授权码的存储方式
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        if (authorizationCodeServices == null) {
            authorizationCodeServices = new AuthenticationCodeServiceImpl(redisConnectionFactory);
        }
        return authorizationCodeServices;
    }

    /**
     * 自定义应用系统信息管理
     */
    private ClientDetailsService clientDetailsService() {
        if (clientDetailsService == null) {
            clientDetailsService = new ClientDetailsServiceImpl();
            clientDetailsService.setPasswordEncoder(passwordEncoder);
            clientDetailsService.setAppClient(appClient);
        }
        return clientDetailsService;
    }

    /**
     * 声明AccessToken管理服务
     */
    private AuthorizationServerTokenServices tokenServices() {
        if (tokenServices == null) {
            tokenServices = new DefaultTokenServices();
        }
        return tokenServices;
    }

    /**
     * 配置应用系统管理，基于{@link ClientDetailsServiceImpl}自定义应用系统信息管理
     */
    @Override
    @SneakyThrows
    public void configure(ClientDetailsServiceConfigurer clients) {
        clients.withClientDetails(clientDetailsService());
    }

    /**
     * 配置获取AccessToken请求的访问权限
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.tokenKeyAccess("permitAll()").allowFormAuthenticationForClients().checkTokenAccess("permitAll()");
    }

    /**
     * 自定义Auth2.0协议确认授权以及认证请求链接
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .authenticationManager(authenticationManager)
                .authorizationCodeServices(authorizationCodeServices())
                .tokenServices(tokenServices())
                .allowedTokenEndpointRequestMethods(HttpMethod.POST)
                .pathMapping("/oauth/confirm_access", "/oauth/confirmAccess")
                .pathMapping("/oauth/authorize", "/oauth/customAuthorize")
        ;
    }

}
