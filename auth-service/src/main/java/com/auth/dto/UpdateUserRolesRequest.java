package com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRolesRequest {
    @Schema(description = "Danh sách role mới", example = "[\"ROLE_EMPLOYEE\",\"ROLE_MANAGER\"]")
    private Set<String> roles;
}
