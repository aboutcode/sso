package com.codeiy.auth.oauth.config;

import com.codeiy.auth.oauth.filter.AuthenticationProcessingFilter;
import com.codeiy.auth.oauth.filter.TokenAuthenticationFilter;
import com.codeiy.auth.oauth.handler.OAuthFailureHandler;
import com.codeiy.auth.oauth.handler.OAuthSuccessHandler;
import com.codeiy.auth.oauth.handler.SsoLogoutSuccessHandler;
import com.codeiy.auth.oauth.service.UserDetailsServiceImpl;
import com.codeiy.core.constant.AuthConstants;
import com.codeiy.user.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;

/**
 * SpringSecurity基础配置
 * 声明认证管理器{@link AuthenticationManager}，认证阶段的用户身份鉴别使用自定义的{@link UserDetailsService}
 * 采用{@link BCryptPasswordEncoder}对登录密码进行加密及编码，{@link BCryptPasswordEncoder}基于随机盐+密钥对密码进行加密，并通过SHA-256算法进行编码
 *
 * @author free@codeiy.com
 */
@Primary
@Order(90)
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {
    private final UserClient userClient;
    /**
     * 基于随机盐+密钥对密码进行加密，并通过SHA-256算法进行编码
     */
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 自定义身份认证服务
     */
    @Override
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl();
        userDetailsService.setUserClient(userClient);
        userDetailsService.setPasswordEncoder(passwordEncoder);
        return userDetailsService;
    }

    /**
     * 1. 禁用csrf
     * 2. 请求会话采用无状态方式
     * 3. 自定义登录登出路径
     * 4. 自定义登录请求参数{@link UsernamePasswordAuthenticationFilter}
     *
     * @param http http请求安全相关配置
     */
    @Override
    @SneakyThrows
    protected void configure(HttpSecurity http) {
        Filter loginFilter = loginFilter();
        Filter tokenAuthenticationFilter = tokenAuthenticationFilter();
        http.formLogin()
                .loginProcessingUrl(AuthConstants.LOGIN_PROCESSING_URL).permitAll()
                .and().csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().logout().logoutSuccessHandler(new SsoLogoutSuccessHandler())
                .and().authorizeRequests()
                .antMatchers(AuthConstants.LOGOUT_URL).permitAll()
                .anyRequest().authenticated()
                // 登录请求参数除了用户名，密码之外，还有验证码等其他参数，通过过滤器自定义认证逻辑
                .and().addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)
                // 解决微服务架构无状态请求场景下，如何识别当前请求所属用户，并绑定到SpringSecurity上下文
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    }

    /**
     * 认证管理器
     */
    @Bean
    @Override
    @SneakyThrows
    public AuthenticationManager authenticationManagerBean() {
        return super.authenticationManagerBean();
    }

    /**
     * 登录密码编码工具
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    /**
     * 自定义登录认证过滤器，满足登录请求参数个性化，多样化需求
     */
    private Filter loginFilter() throws Exception {
        AuthenticationProcessingFilter loginFilter = new AuthenticationProcessingFilter();
        loginFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(AuthConstants.LOGIN_PROCESSING_URL, AuthConstants.METHOD_POST));
        loginFilter.setAuthenticationSuccessHandler(new OAuthSuccessHandler());
        loginFilter.setAuthenticationFailureHandler(new OAuthFailureHandler());
        loginFilter.setAuthenticationManager(authenticationManager());
        return loginFilter;
    }

    /**
     * token解析过滤器，解决微服务无状态场景下，Spring Security OAuth无法在Session中获取用户认证信息的问题
     */
    private Filter tokenAuthenticationFilter() {
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter();
        return tokenAuthenticationFilter;
    }
}
