package org.cloudstorage.service.file;

import lombok.RequiredArgsConstructor;
import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.entity.User;
import org.cloudstorage.repository.FileNodeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileManagementService {
    private final FileNodeRepository fileNodeRepository;

    public FileNode findDirectoryByPath(User owner, String directoryPath) {
        if (directoryPath == null || directoryPath.isBlank()) {
            throw new IllegalArgumentException("Directory path is empty");
        }

        if ("/".equals(directoryPath)) {
            return null;
        }

        if (!directoryPath.endsWith("/")) {
            throw new IllegalArgumentException("Directory path must end with '/'");
        }

        String trimmed = directoryPath.substring(1, directoryPath.length() - 1);
        String[] parts = trimmed.split("/");

        FileNode current = null;
        for (String part : parts) {
            current = (FileNode) fileNodeRepository.findByOwnerAndParentAndName(owner, current, part)
                    .orElseThrow(() -> new IllegalArgumentException("Directory not found: " + directoryPath));

            if (!current.isDirectory()) {
                throw new IllegalArgumentException("Path part is not a directory: " + part);
            }
        }

        return current;
    }
}
