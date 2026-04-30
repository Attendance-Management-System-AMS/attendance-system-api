package com.attendance.client;

import com.attendance.dto.request.InternalCreateUserRequest;
import com.attendance.dto.request.InternalUpdateUserRequest;
import com.attendance.dto.response.InternalUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PostMapping("/internal/auth/users")
    InternalUserResponse createUser(@RequestBody InternalCreateUserRequest request);

    @PutMapping("/internal/auth/users/{id}")
    InternalUserResponse updateUser(@PathVariable("id") Long id, @RequestBody InternalUpdateUserRequest request);
}
