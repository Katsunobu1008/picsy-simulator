package com.example.picsy_engine.api.dto;

import jakarta.validation.constraints.NotBlank;

/** POST /api/members : 新メンバー追加 */
public record AddMemberRequest(@NotBlank String name) {}
