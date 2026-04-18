package org.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.cloudstorage.dto.ResourceDto;
import org.cloudstorage.mapper.ResourceMapper;
import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.security.UserDetails;
import org.cloudstorage.service.FileNodeService;
import org.cloudstorage.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/resource")
public class ResourceController {

    private final StorageService storageService;
    private final FileNodeService fileNodeService;

    @GetMapping
    public ResponseEntity<ResourceDto> getResource(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validatePath(path);
        FileNode node = fileNodeService.getResource(path, userDetails.getId());
        return ResponseEntity.ok(ResourceMapper.toDto(node));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceDto>> upload(
            @RequestParam String path,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validatePath(path);

        List<ResourceDto> uploadedResources = files.stream()
                .map(file -> storageService.uploadFile(file, path, userDetails.getId()))
                .map(ResourceMapper::toDto)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedResources);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        validatePath(path);
        return storageService.downloadResource(path, userDetails.getId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        validatePath(path);
        fileNodeService.deleteResource(path, userDetails.getId());
    }

    @GetMapping("/move")
    public ResponseEntity<ResourceDto> move(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ResourceDto result = fileNodeService.moveResource(from, to, userDetails.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceDto>> search(
            @RequestParam("query") String query,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<ResourceDto> results = fileNodeService.search(query, userDetails.getId());
        return ResponseEntity.ok(results);
    }

    private void validatePath(String path) {
        if (path == null || path.isEmpty()) {
            assert path != null;
            throw new InvalidPathException(path, "Path cannot be empty");
        }
    }
}