package com.attendance.controller;

import com.attendance.dto.request.InternalCreateUserRequest;
import com.attendance.dto.response.InternalUserResponse;
import com.attendance.service.AuthService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final AuthService authService;

    @PostMapping("/users")
    public InternalUserResponse createUser(@Valid @RequestBody InternalCreateUserRequest request) {
        return authService.createInternalUser(request);
    }
}
