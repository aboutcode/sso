package com.codeiy.user.client;


import com.codeiy.user.dto.AppDTO;
import com.codeiy.core.constant.ServiceNameConstants;
import com.codeiy.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "appClient", value = ServiceNameConstants.USER_SERVICE)
public interface AppClient {
    @RequestMapping("/app/getByClientId")
    R<AppDTO> getByClientId(@RequestParam("clientId") String clientId);
}
