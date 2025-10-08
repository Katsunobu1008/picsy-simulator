package com.example.picsy_engine.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/** POST /api/recovery : gamma (0..1) */
public record RecoveryRequest(
        @DecimalMin("0.0") @DecimalMax("1.0") double gamma
) {}
