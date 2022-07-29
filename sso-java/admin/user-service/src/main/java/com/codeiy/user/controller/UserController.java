package com.codeiy.user.controller;

import com.codeiy.core.util.R;
import com.codeiy.user.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/getByUsername")
    public R<UserDTO> getByUsername(String username) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("admin");
        userDTO.setPassword("admin");
        userDTO.setAuthorities(Arrays.asList("userInfo", "openid"));
        userDTO.setToken("123456");
        return R.ok(userDTO);
    }
}
