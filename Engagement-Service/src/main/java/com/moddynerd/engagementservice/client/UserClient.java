package com.moddynerd.engagementservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/user/batch")
    Map<String, String> getUsernames(@RequestBody List<String> userIds);

}
