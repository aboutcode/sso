package com.codeiy.auth.oauth.handler;

import cn.hutool.json.JSONUtil;
import com.codeiy.core.constant.AuthConstants;
import com.codeiy.core.util.R;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class OAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        PrintWriter out = null;
        try {
            response.setCharacterEncoding(AuthConstants.UTF8);
            response.setContentType(AuthConstants.CONTENT_TYPE_JSON);
            out = response.getWriter();
            out.println(JSONUtil.toJsonStr(R.failed("认证失败")));
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
}
