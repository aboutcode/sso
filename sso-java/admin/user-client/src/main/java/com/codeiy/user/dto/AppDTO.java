package com.codeiy.user.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDTO implements Serializable {
    public static final Integer AUTO_APPROVE_YES = 1;
    private String clientId;
    private String clientSecret;
    /**
     * 授权类型（可选值：authorization_code, password, client_credentials, implicit, refresh_token）
     */
    private String authorizedGrantTypes;
    /**
     * 授权范围（可选值：openid, profile）
     */
    private String scopes;
    /**
     * 是否自动授权
     */
    private Integer autoApprove;
    /**
     * 令牌有效期, 单位：秒
     */
    private Integer accessTokenExpire;
    /**
     * 刷新令牌有效期, 单位：秒
     */
    private Integer refreshTokenExpire;
    /**
     * 回调地址
     */
    private String redirectUrl;
}
