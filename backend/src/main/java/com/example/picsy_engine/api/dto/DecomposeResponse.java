package com.example.picsy_engine.api.dto;

import java.util.List;

/** GET /api/companies/{id}/decompose : 仮想解体の結果（人だけの行列） */
public record DecomposeResponse(
        List<MemberView> people,
        double[][] matrix
) {}
