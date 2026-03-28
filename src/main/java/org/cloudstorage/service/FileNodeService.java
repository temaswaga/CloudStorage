package org.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.entity.User;
import org.cloudstorage.repository.FileNodeRepository;
import org.cloudstorage.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
public class FileNodeService {

    private final FileNodeRepository fileNodeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FileNode getResource(String path, Long userId) {
        User owner = userRepository.getReferenceById(userId);
        FileNode node = resolveExistingPath(path, owner);
        initializeParentChain(node.getParent());
        return node;
    }

    @Transactional
    public void deleteResource(String path, Long userId) {
        User owner = userRepository.getReferenceById(userId);
        FileNode node = resolveExistingPath(path, owner);
        fileNodeRepository.delete(node);
    }

    @Transactional(readOnly = true)
    public List<FileNode> listDirectory(String path, Long userId) {
        User owner = userRepository.getReferenceById(userId);

        FileNode parent = null;
        if (path != null && !path.isBlank() && !path.equals("/")) {
            String dirPath = path.endsWith("/") ? path : path + "/";
            parent = resolveExistingPath(dirPath, owner);
        }

        return fileNodeRepository.findByOwnerAndParent(owner, parent);
    }

    @Transactional
    public FileNode createDirectory(String path, Long userId) {
        if (path == null || path.isBlank() || !path.endsWith("/")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        User owner = userRepository.getReferenceById(userId);

        String stripped = path.replaceAll("^/+|/+$", "");
        int lastSlash = stripped.lastIndexOf("/");

        String parentPath;
        if (lastSlash >= 0) {
            parentPath = stripped.substring(0, lastSlash);
        } else {
            parentPath = null;
        }

        String dirName;
        if (lastSlash >= 0) {
            dirName = stripped.substring(lastSlash + 1);
        } else {
            dirName = stripped;
        }

        FileNode parent = null;
        if (parentPath != null && !parentPath.isBlank()) {
            parent = resolveExistingPath(parentPath + "/", owner);
        }

        if (fileNodeRepository.findByOwnerAndParentAndName(owner, parent, dirName).isPresent()) {
            throw new IllegalStateException("Directory already exists: " + path);
        }

        FileNode dir = new FileNode();
        dir.setName(dirName);
        dir.setDirectory(true);
        dir.setOwner(owner);
        dir.setParent(parent);

        FileNode saved = fileNodeRepository.save(dir);
        initializeParentChain(saved.getParent());
        return saved;
    }

    @Transactional
    public FileNode saveFileNode(String fileName, Long sizeBytes, String objectKey, String path, Long userId) {
        User owner = userRepository.getReferenceById(userId);
        FileNode parent = resolveParentPath(path, owner);

        FileNode node = new FileNode();
        node.setName(fileName);
        node.setDirectory(false);
        node.setSizeBytes(sizeBytes);
        node.setS3Key(objectKey);
        node.setOwner(owner);
        node.setParent(parent);

        return fileNodeRepository.save(node);
    }

    public FileNode resolveExistingPath(String path, User owner) {
        boolean isDirectory = path.endsWith("/");
        String stripped = path.replaceAll("^/+", "").replaceAll("/+$", "");

        if (stripped.isBlank()) {
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

            if (!isLast && !node.isDirectory()) {
                throw new IllegalArgumentException("Not a directory: " + segment);
            }

            current = node;
        }

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

        return current;
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

    public void initializeParentChain(FileNode node) {
        while (node != null) {
            Hibernate.initialize(node);
            node = node.getParent();
        }
    }
}