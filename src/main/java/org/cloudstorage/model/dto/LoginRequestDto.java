package org.cloudstorage.model.dto;

public record LoginRequestDto (
        String username,
        String password
) {}
