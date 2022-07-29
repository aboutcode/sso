package com.codeiy.auth.oauth.service;

import com.codeiy.core.constant.AuthConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;

@Slf4j
public class AuthenticationCodeServiceImpl extends RandomValueAuthorizationCodeServices {

    private RedisConnectionFactory connectionFactory;
    private String key = AuthConstants.OAUTH_PREFIX + AuthConstants.OAUTH_CODE_KEY;

    public AuthenticationCodeServiceImpl(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected OAuth2Authentication remove(String code) {
        RedisConnection conn = connectionFactory.getConnection();
        try {
            OAuth2Authentication authentication;
            try {
                authentication = SerializationUtils.deserialize(conn.hGet(key.getBytes(AuthConstants.UTF8), code.getBytes(AuthConstants.UTF8)));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }

            if (null != authentication) {
                conn.hDel(key.getBytes(AuthConstants.UTF8), code.getBytes(AuthConstants.UTF8));
            }

            return authentication;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            conn.close();
        }
    }

    /**
     * 保存授权码
     *
     * @param code 授权码
     */
    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        RedisConnection conn = connectionFactory.getConnection();
        try {
            conn.hSet(key.getBytes(AuthConstants.UTF8), code.getBytes(AuthConstants.UTF8), SerializationUtils.serialize(authentication));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            conn.close();
        }

    }
}
