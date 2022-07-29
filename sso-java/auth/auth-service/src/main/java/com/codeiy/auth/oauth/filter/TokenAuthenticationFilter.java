package com.codeiy.auth.oauth.filter;

import com.codeiy.user.dto.UserDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析认证成功后的token信息，将用户信息放入security上下文
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse
            httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String token = httpServletRequest.getHeader("Authorization");
        if (token != null) {
            // Map<String, Object> userParams = parseToken(token);
            Map<String, Object> userParams = new HashMap<>();
            userParams.put("username", "admin");
            userParams.put("authorities", "profile,openid");
            UserDTO user = new UserDTO();
            user.setUsername(userParams.get("username").toString());
            String[] authorities = userParams.get("authorities").toString().split(",");
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.createAuthorityList(authorities));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(
                    httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
