package com.attendance.controller;

import com.common.dto.ApiResponse;
import com.attendance.dto.holiday.HolidayRequest;
import com.attendance.dto.holiday.HolidayResponse;
import com.attendance.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/holidays")
@RequiredArgsConstructor
@Tag(name = "Chấm công - Ngày nghỉ", description = "Quản lý ngày nghỉ lễ và nghỉ hưởng lương")
public class HolidayController {

    private final HolidayService holidayService;

    // Lấy toàn bộ danh sách ngày nghỉ.
    @GetMapping
    @Operation(summary = "Lấy danh sách ngày nghỉ")
    public ApiResponse<List<HolidayResponse>> getHolidays() {
        return ApiResponse.success("Lấy danh sách ngày nghỉ thành công", holidayService.getAll());
    }

    // Tìm kiếm ngày nghỉ theo bộ lọc và phân trang.
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm ngày nghỉ (filter + paging)")
    public ApiResponse<Page<HolidayResponse>> search(
            @Parameter(description = "Từ khoá theo tên ngày nghỉ")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "Có hưởng lương hay không")
            @RequestParam(value = "isPaid", required = false) Boolean isPaid,
            @Parameter(description = "Từ ngày (yyyy-MM-dd)")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Đến ngày (yyyy-MM-dd)")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20, sort = "fromDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.success(
                "Tìm kiếm ngày nghỉ thành công",
                holidayService.search(keyword, isPaid, fromDate, toDate, pageable));
    }

    // Tạo mới một ngày nghỉ.
    @PostMapping
    @Operation(summary = "Tạo ngày nghỉ")
    public ApiResponse<HolidayResponse> createHoliday(@Valid @RequestBody HolidayRequest request) {
        HolidayResponse response = holidayService.create(request);
        return ApiResponse.success("Tạo ngày nghỉ thành công", response);
    }

    // Lấy chi tiết ngày nghỉ theo ID.
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết ngày nghỉ theo ID")
    public ApiResponse<HolidayResponse> getHolidayById(
            @Parameter(description = "ID ngày nghỉ") @PathVariable Long id) {
        return ApiResponse.success("Lấy ngày nghỉ thành công", holidayService.getById(id));
    }

    // Cập nhật thông tin ngày nghỉ.
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật ngày nghỉ")
    public ApiResponse<HolidayResponse> updateHoliday(
            @Parameter(description = "ID ngày nghỉ") @PathVariable Long id,
            @Valid @RequestBody HolidayRequest request) {
        return ApiResponse.success("Cập nhật ngày nghỉ thành công", holidayService.update(id, request));
    }

    // Xóa ngày nghỉ theo ID.
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa ngày nghỉ")
    public ApiResponse<Void> deleteHoliday(
            @Parameter(description = "ID ngày nghỉ") @PathVariable Long id) {
        holidayService.delete(id);
        return ApiResponse.success("Xóa ngày nghỉ thành công", null);
    }
}
