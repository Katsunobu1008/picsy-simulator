package com.example.picsy_engine.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** POST /api/transactions : 定価取引 */
public record TransactionRequest(
        @NotNull Integer buyerId,
        @NotNull Integer sellerId,
        @Min(0) double price
) {}
