package org.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.cloudstorage.model.entity.FileNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioService minioService;
    private final FileNodeService fileNodeService;

    @Transactional
    public FileNode uploadFile(MultipartFile file, String path, Long userId) {
        String objectKey = minioService.upload(file);

        try {
            return fileNodeService.saveFileNode(
                    file.getOriginalFilename(),
                    file.getSize(),
                    objectKey,
                    path,
                    userId
            );
        } catch (Exception e) {
            // TODO minioService.delete(objectKey);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> downloadResource(final String path, final Long userId) throws IOException {
        final FileNode node = fileNodeService.getResource(path, userId);

        if (node.isDirectory()) {
            return downloadDirectoryAsZip(node);
        } else {
            return downloadSingleFile(node);
        }
    }

    private ResponseEntity<StreamingResponseBody> downloadSingleFile(final FileNode node) {
        StreamingResponseBody responseBody =
                outputStream -> {
                    try (InputStream minioStream = minioService.download(node.getS3Key())) {
                        minioStream.transferTo(outputStream);
                    }
                };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + node.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    private ResponseEntity<StreamingResponseBody> downloadDirectoryAsZip(final FileNode dir) {
        StreamingResponseBody responseBody =
                outputStream -> {
                    try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                        zipNode(zos, dir, "");
                    }
                };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dir.getName() + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    private void zipNode(ZipOutputStream zos, FileNode node, String prefix) throws IOException {
        String currentPathInZip = prefix + node.getName();

        if (node.isDirectory()) {
            String pathWithSlash = currentPathInZip + "/";

            List<FileNode> children = fileNodeService.listDirectory(pathWithSlash, node.getOwner().getId());

            for (FileNode child : children) {
                zipNode(zos, child, pathWithSlash);
            }
        } else {
            try (InputStream is = minioService.download(node.getS3Key())) {
                zos.putNextEntry(new ZipEntry(currentPathInZip));
                is.transferTo(zos);
                zos.closeEntry();
            } catch (Exception e) {
                throw new IOException("Failed to zip file: " + node.getName());
            }
        }
    }
}