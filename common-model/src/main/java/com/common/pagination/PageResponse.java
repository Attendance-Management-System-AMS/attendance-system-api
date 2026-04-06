package com.common.pagination;
 
import org.springframework.data.domain.Page;
import java.util.List;
 
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}
