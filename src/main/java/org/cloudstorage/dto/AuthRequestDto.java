package org.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRequestDto(
        @Schema(description = "Имя пользователя", example = "ivan_ivanov")
        String username,

        @Schema(description = "Пароль", example = "strong_password_123")
        String password
) {}