package org.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDto (
        @Schema(description = "Имя пользователя", example = "ivan_ivanov")
        String username
) {}
