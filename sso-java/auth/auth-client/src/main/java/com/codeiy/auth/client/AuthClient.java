package com.codeiy.auth.client;


import com.codeiy.core.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "authClient", value = ServiceNameConstants.AUTH_SERVICE)
public interface AuthClient {
}
