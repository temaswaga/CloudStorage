package org.cloudstorage.model.dto;

public record RegistrationRequestDto(
        String password,
        String username
) {}
