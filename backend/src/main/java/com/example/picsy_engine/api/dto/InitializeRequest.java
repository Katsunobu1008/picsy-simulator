package com.example.picsy_engine.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 初期化 API の入力 DTO。
 * - 今回は names は常に null を送る前提（バックエンドは null/不一致時に A,B,C... を自動採番）。
 * - matrix: 必須。正方 NxN、非負。サーバ側で Kahan 和＋残差集約で正規化。
 */
public record InitializeRequest(
        List<String> names,
        @NotNull double[][] matrix
) {}