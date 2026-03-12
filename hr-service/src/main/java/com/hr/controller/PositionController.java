package com.hr.controller;

import com.common.dto.ApiResponse;
import com.hr.dto.position.PositionRequest;
import com.hr.dto.position.PositionResponse;
import com.hr.service.PositionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping
    public ApiResponse<PositionResponse> createPosition(@Valid @RequestBody PositionRequest request) {
        PositionResponse response = positionService.create(request);
        return ApiResponse.success(201, "Tạo chức vụ thành công", response);
    }

    @GetMapping
    public ApiResponse<List<PositionResponse>> getPositions(@RequestParam(required = false) Long departmentId) {
        return ApiResponse.success(positionService.getAll(departmentId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PositionResponse> getPositionById(@PathVariable Long id) {
        return ApiResponse.success(positionService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PositionResponse> updatePosition(@PathVariable Long id,
                                                        @Valid @RequestBody PositionRequest request) {
        PositionResponse response = positionService.update(id, request);
        return ApiResponse.success(200, "Cập nhật chức vụ thành công", response);
    }
}
