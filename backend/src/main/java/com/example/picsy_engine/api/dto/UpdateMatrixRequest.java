package com.example.picsy_engine.api.dto;

import jakarta.validation.constraints.NotNull;

/** PUT /api/matrix の入力：行列 */
public record UpdateMatrixRequest(@NotNull double[][] matrix) {}
