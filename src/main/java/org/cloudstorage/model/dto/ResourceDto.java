package org.cloudstorage.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ResourceDto (
        String path,
        String name,
        Long size,
        String type
){}
