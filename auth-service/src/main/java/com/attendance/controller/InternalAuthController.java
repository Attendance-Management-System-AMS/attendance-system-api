package com.attendance.controller;

import com.attendance.dto.request.InternalCreateUserRequest;
import com.attendance.dto.request.InternalUpdateUserRequest;
import com.attendance.dto.response.InternalUserResponse;
import com.attendance.service.AuthService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/users/{id}")
    public InternalUserResponse updateUser(@PathVariable("id") Long id, @Valid @RequestBody InternalUpdateUserRequest request) {
        return authService.updateInternalUser(id, request);
    }
}
