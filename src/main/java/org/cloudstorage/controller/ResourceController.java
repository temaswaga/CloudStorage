package org.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cloudstorage.mapper.ResourceMapper;
import org.cloudstorage.dto.ResourceDto;
import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.security.UserDetails;
import org.cloudstorage.service.FileNodeService;
import org.cloudstorage.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ResourceController {

    private final StorageService storageService;
    private final FileNodeService fileNodeService;

    @GetMapping("/resource")
    public ResponseEntity<?> getResource(
            @RequestParam("path") String path,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        FileNode node = fileNodeService.getResource(path, userDetails.getId());
        return ResponseEntity.ok(ResourceMapper.toDto(node));
    }

    @PostMapping(path = "/resource", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(
            @RequestParam("path") String path,
            HttpServletRequest request,
            Authentication authentication
    ) {

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        if (!(request instanceof MultipartHttpServletRequest multipartRequest)) {
            return ResponseEntity.status(400).build();
        }

        //file already exists

        List<ResourceDto> result = new ArrayList<>();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        for (Map.Entry<String, List<MultipartFile>> entry : multipartRequest.getMultiFileMap().entrySet()) {
            for (MultipartFile file : entry.getValue()) {
                log.info("File: {}", file.getOriginalFilename());
                FileNode node = storageService.uploadFile(
                        file,
                        path,
                        userDetails.getId()
                );
                result.add(ResourceMapper.toDto(node));
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<Resource> download(
            @RequestParam("path") String path,
            Authentication authentication
    ) throws IOException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return storageService.downloadResource(path, userDetails.getId());
    }

    @DeleteMapping(path = "/resource")
    public ResponseEntity<?> deleteFile(@RequestParam("path") String path,
                                        Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        fileNodeService.deleteResource(path, userDetails.getId());
        return ResponseEntity.status(204).build();
    }
}