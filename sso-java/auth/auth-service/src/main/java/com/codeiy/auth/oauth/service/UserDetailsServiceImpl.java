package com.codeiy.auth.oauth.service;

import cn.hutool.json.JSONUtil;
import com.codeiy.core.util.R;
import com.codeiy.user.client.UserClient;
import com.codeiy.user.dto.UserDTO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    @Setter
    private UserClient userClient;
    @Setter
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userInfo) throws UsernameNotFoundException {
        Map<String, Object> requestParams = JSONUtil.parseObj(userInfo);
        UserDTO user = loadUserInfo(requestParams);

        return User.withUsername(user.getUsername()).password(passwordEncoder.encode(user.getPassword()))
                .authorities(user.getAuthorities().toArray(new String[]{})).build();
    }

    private UserDTO loadUserInfo(Map<String, Object> userInfo) {
        R<UserDTO> response = userClient.getByUsername(userInfo.get("username").toString());
        return response.getData();
    }
}
