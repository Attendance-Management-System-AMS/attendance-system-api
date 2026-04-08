package com.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeInternalResponse {
    private Long userId;
    private String fullName;
    private String departmentName;
    private String positionName;
}
