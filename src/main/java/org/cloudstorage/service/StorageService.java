package org.cloudstorage.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.entity.User;
import org.cloudstorage.repository.FileNodeRepository;
import org.cloudstorage.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient minioClient;
    private final FileNodeRepository fileNodeRepository;
    private final UserRepository userRepository;

    @Transactional
    public FileNode uploadFile(MultipartFile file, String path, Long userId) {
        try {
            String objectKey = UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("files")
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            User owner = userRepository.getReferenceById(userId);
            FileNode parent = resolveParentPath(path, owner);

            FileNode node = new FileNode();
            node.setName(file.getOriginalFilename());
            node.setDirectory(false);
            node.setSizeBytes(file.getSize());
            node.setS3Key(objectKey);
            node.setOwner(owner);
            node.setParent(parent);

            FileNode saved = fileNodeRepository.save(node);
            log.info("SAVED: {}", saved.getId());
            return saved;

        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    private FileNode resolveParentPath(String path, User owner) {
        if (path == null || path.isBlank() || path.equals("/")) {
            return null;
        }

        String[] segments = path.strip().replaceAll("^/+|/+$", "").split("/");

        FileNode current = null;
        for (String segment : segments) {
            if (segment.isBlank()) continue;
            FileNode finalCurrent = current;
            current = fileNodeRepository
                    .findByOwnerAndParentAndName(owner, current, segment)
                    .orElseGet(() -> {
                        FileNode dir = new FileNode();
                        dir.setName(segment);
                        dir.setDirectory(true);
                        dir.setOwner(owner);
                        dir.setParent(finalCurrent);
                        return fileNodeRepository.save(dir);
                    });
        }
        return current;
    }

    @Transactional(readOnly = true)
    public FileNode getResource(String path, Long userId) {
        User owner = userRepository.getReferenceById(userId);

        // Убираем leading слэш, разбиваем на сегменты
        // "folder1/folder2/file.txt" -> ["folder1", "folder2", "file.txt"]
        // "folder1/folder2/"         -> ["folder1", "folder2"]  (папка)
        boolean isDirectory = path.endsWith("/");
        String stripped = path.replaceAll("^/+", "").replaceAll("/+$", "");

        if (stripped.isBlank()) {
            // Запросили корень "/"
            throw new IllegalArgumentException("Root directory info is not supported");
        }

        String[] segments = stripped.split("/");

        FileNode current = null;
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            boolean isLast = i == segments.length - 1;

            FileNode node = fileNodeRepository
                    .findByOwnerAndParentAndName(owner, current, segment)
                    .orElseThrow(() -> new NoSuchElementException("Resource not found: " + segment));

            // Если промежуточный сегмент оказался файлом — путь некорректный
            if (!isLast && !node.isDirectory()) {
                throw new IllegalArgumentException("Not a directory: " + segment);
            }

            current = node;
        }

        // Проверяем что тип совпадает с тем что запросили
        if (isDirectory) {
            assert current != null;
            if (!current.isDirectory()) {
                throw new IllegalArgumentException("Resource is a file, not a directory");
            }
        }
        if (!isDirectory) {
            assert current != null;
            if (current.isDirectory()) {
                throw new IllegalArgumentException("Resource is a directory, not a file");
            }
        }

        initializeParentChain(current.getParent());
        return current;
    }

    private void initializeParentChain(FileNode node) {
        while (node != null) {
            Hibernate.initialize(node);
            node = node.getParent();
        }
    }
}
