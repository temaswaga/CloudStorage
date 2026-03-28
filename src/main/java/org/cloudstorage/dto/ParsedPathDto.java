package org.cloudstorage.dto;

public record ParsedPathDto(
        String normalizedPath,
        String parentPath,
        String name,
        boolean directory
) {}

