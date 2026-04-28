package com.attendance.dto.request;

import com.attendance.util.FaceEmbeddingUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record FaceDescriptorRequest(
    @NotEmpty(message = "Descriptor khuôn mặt là bắt buộc")
    @Size(min = FaceEmbeddingUtils.FACE_DESCRIPTOR_LENGTH, max = FaceEmbeddingUtils.FACE_DESCRIPTOR_LENGTH,
            message = "Descriptor phải có đúng 128 phần tử")
    List<Double> descriptor
) {}

