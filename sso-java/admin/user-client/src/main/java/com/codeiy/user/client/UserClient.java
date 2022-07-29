package com.codeiy.user.client;

import com.codeiy.user.dto.UserDTO;
import com.codeiy.core.constant.ServiceNameConstants;
import com.codeiy.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "userClient", value = ServiceNameConstants.USER_SERVICE)
public interface UserClient {
    @RequestMapping("/user/getByUsername")
    R<UserDTO> getByUsername(@RequestParam("username") String username);
}
