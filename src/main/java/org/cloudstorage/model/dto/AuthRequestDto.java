package org.cloudstorage.model.dto;

public record AuthRequestDto(
        String username,
        String password
) {}