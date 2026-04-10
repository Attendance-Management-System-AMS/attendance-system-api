package com.attendance.dto.request;

import java.util.List;

public record FaceDescriptorRequest(
    List<Double> descriptor
) {}

