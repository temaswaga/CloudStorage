package org.cloudstorage.dto;

public record AuthRequestDto(
        String username,
        String password
) {}