package org.cloudstorage.model.dto;

public record ParsedPathDto(
        String normalizedPath,
        String parentPath,
        String name,
        boolean directory
) {}

