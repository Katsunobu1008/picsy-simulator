package com.example.picsy_engine.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** POST /api/companies : 会社設立 */
public record CompanyCreateRequest(
        @NotBlank String name,
        @DecimalMin("0.0") @DecimalMax("0.9") double budget,
        @NotNull List<FounderInvest> founders,
        @NotNull List<CompanyOutflow> outflows
){
    public record FounderInvest(@NotNull Integer memberId, @DecimalMin("0.0") double invest){}
    public record CompanyOutflow(@NotNull Integer memberId, @DecimalMin("0.0") double weight){}
}
