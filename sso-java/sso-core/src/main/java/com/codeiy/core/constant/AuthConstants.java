package com.codeiy.core.constant;

public interface AuthConstants {
    /**
     * 登录请求的用户字段名称
     */
    String FORM_USERNAME_KEY = "username";
    /**
     * 登录请求的密码字段名称
     */
    String FORM_PASSWORD_KEY = "password";
    /**
     * 认证授权相关缓存，设置统一的前缀
     */
    String OAUTH_PREFIX = "OAuth2.0:";
    /**
     * 认证授权相关缓存，授权码
     */
    String OAUTH_CODE_KEY = "auth_code:";
    /**
     * UTF-8编码
     */
    String UTF8 = "UTF-8";
    /**
     * contentType
     */
    String CONTENT_TYPE_JSON = "application/json";
    /**
     * SpringSecurity登录请求
     */
    String LOGIN_PROCESSING_URL = "/oauth/login";
    /**
     * SpringSecurity退出登录请求
     */
    String LOGOUT_URL = "/oauth/logout";
    /**
     * POST方法
     */
    String METHOD_POST = "POST";
    /**
     * OIDC协议授权域
     */
    String SCOPE_OPENID = "openid";
    /**
     * OAuth2.0协议授权域
     */
    String SCOPE_PROFILE = "profile";
}
