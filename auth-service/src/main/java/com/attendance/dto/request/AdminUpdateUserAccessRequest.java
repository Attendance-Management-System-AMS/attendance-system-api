package com.attendance.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record AdminUpdateUserAccessRequest(
        @NotEmpty(message = "Phải có ít nhất một vai trò")
        Set<String> roles,
        boolean enabled
) {
}
