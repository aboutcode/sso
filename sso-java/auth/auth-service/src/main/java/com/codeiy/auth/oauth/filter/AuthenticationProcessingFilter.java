package com.codeiy.auth.oauth.filter;

import cn.hutool.json.JSONUtil;
import com.codeiy.core.constant.AuthConstants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationProcessingFilter extends UsernamePasswordAuthenticationFilter {
    private boolean postOnly = true;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String userName = request.getParameter(AuthConstants.FORM_USERNAME_KEY);
        String password = request.getParameter(AuthConstants.FORM_PASSWORD_KEY);

        if (userName == null) {
            userName = "";
        }
        if (password == null) {
            password = "";
        }


        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", userName);
        userInfo.put("password", password);
        userName = JSONUtil.toJsonStr(userInfo);
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userName, password);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return super.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    protected void setDetails(HttpServletRequest request,
                              UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }
}
