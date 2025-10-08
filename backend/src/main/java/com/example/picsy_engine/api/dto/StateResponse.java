package com.example.picsy_engine.api.dto;

import java.util.List;

/** /api/state の応答：メンバー、行列、貢献度、購買力 */
public record StateResponse(
        List<MemberView> members,
        double[][] matrix,
        double[] contributions,
        double[] purchasingPower
) {}
