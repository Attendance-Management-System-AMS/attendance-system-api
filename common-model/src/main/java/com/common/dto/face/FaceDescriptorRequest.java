package com.common.dto.face;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Descriptor 128 chiều từ face-api.js ({@code faceapi.FaceRecognitionNet}).
 */
public record FaceDescriptorRequest(
        @NotNull(message = "descriptor là bắt buộc")
        @Size(min = 128, max = 128, message = "descriptor phải đúng 128 phần tử (face-api.js FaceRecognitionNet)")
        List<Double> descriptor
) {
}
