package org.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.cloudstorage.dto.ResourceDto;
import org.cloudstorage.mapper.ResourceMapper;
import org.cloudstorage.model.security.UserDetails;
import org.cloudstorage.service.FileNodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class DirectoryController {
    private final FileNodeService fileNodeService;

    @GetMapping("/directory")
    public ResponseEntity<List<ResourceDto>> list(
            @RequestParam String path,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<ResourceDto> result = fileNodeService.listDirectory(path, userDetails.getId())
                .stream()
                .map(ResourceMapper::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/directory")
    public ResponseEntity<ResourceDto> createDirectory(
            @RequestParam String path,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ResourceDto result = ResourceMapper.toDto(fileNodeService.createDirectory(path, userDetails.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}

