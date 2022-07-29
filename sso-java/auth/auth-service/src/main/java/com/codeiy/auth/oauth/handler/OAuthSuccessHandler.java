package com.codeiy.auth.oauth.handler;

import cn.hutool.json.JSONUtil;
import com.codeiy.core.constant.AuthConstants;
import com.codeiy.core.util.R;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class OAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        PrintWriter out = null;
        try {
            response.setCharacterEncoding(AuthConstants.UTF8);
            response.setContentType(AuthConstants.CONTENT_TYPE_JSON);
            out = response.getWriter();
            out.println(JSONUtil.toJsonStr(R.ok(authentication.getPrincipal())));
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
}
