package org.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cloudstorage.model.entity.FileNode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioService minioService;
    private final FileNodeService fileNodeService;

    public FileNode uploadFile(MultipartFile file, String path, Long userId) {
        String objectKey = minioService.upload(file);
        log.info("Uploaded to MinIO: {}", objectKey);

        FileNode saved = fileNodeService.saveFileNode(
                file.getOriginalFilename(),
                file.getSize(),
                objectKey,
                path,
                userId
        );
        log.info("SAVED: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadResource(String path, Long userId) throws IOException {
        FileNode node = fileNodeService.getResource(path, userId);

        if (node.isDirectory()) {
            byte[] zipBytes = zipDirectory(node);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + node.getName() + ".zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new ByteArrayResource(zipBytes));
        } else {
            InputStream stream = minioService.download(node.getS3Key());
            byte[] bytes = stream.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + node.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new ByteArrayResource(bytes));
        }
    }

    private byte[] zipDirectory(FileNode dir) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zipNode(zos, dir, "");
        }
        return baos.toByteArray();
    }

    private void zipNode(ZipOutputStream zos, FileNode node, String prefix) {
        if (node.isDirectory()) {
            List<FileNode> children = fileNodeService.listDirectory(
                    prefix + node.getName() + "/",
                    node.getOwner().getId()
            );
            for (FileNode child : children) {
                zipNode(zos, child, prefix + node.getName() + "/");
            }
        } else {
            try {
                InputStream stream = minioService.download(node.getS3Key());
                zos.putNextEntry(new ZipEntry(prefix + node.getName()));
                stream.transferTo(zos);
                zos.closeEntry();
            } catch (Exception e) {
                throw new RuntimeException("Failed to zip file: " + node.getName(), e);
            }
        }
    }
}