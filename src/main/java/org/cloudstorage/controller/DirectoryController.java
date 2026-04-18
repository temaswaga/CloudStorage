package org.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.cloudstorage.dto.ResourceDto;
import org.cloudstorage.mapper.ResourceMapper;
import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.security.UserDetails;
import org.cloudstorage.service.FileNodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directory")
public class DirectoryController {

    private final FileNodeService fileNodeService;

    @GetMapping
    public ResponseEntity<List<ResourceDto>> list(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validateDirectoryPath(path);

        List<ResourceDto> result = fileNodeService.listDirectory(path, userDetails.getId())
                .stream()
                .map(ResourceMapper::toDto)
                .toList();

        return ResponseEntity.ok(result);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceDto createDirectory(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validateDirectoryPath(path);

        FileNode newDirectory = fileNodeService.createDirectory(path, userDetails.getId());
        return ResourceMapper.toDto(newDirectory);
    }

    private void validateDirectoryPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        if (!path.isEmpty() && !path.endsWith("/")) {
            throw new IllegalArgumentException("Directory path must end with '/'");
        }
    }
}

