package com.codeiy.user.controller;

import com.codeiy.core.util.R;
import com.codeiy.user.dto.AppDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class AppController {
    @GetMapping("/getByClientId")
    public R<AppDTO> getByClientId(String clientId) {
        AppDTO appDTO = new AppDTO();
        appDTO.setClientId("c1");
        appDTO.setClientSecret("p1");
        appDTO.setScopes("openid");
        appDTO.setRedirectUrl("http://www.baidu.com");
        return R.ok(appDTO);
    }
}
