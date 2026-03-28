package org.cloudstorage.mapper;

import org.cloudstorage.dto.ResourceDto;
import org.cloudstorage.model.entity.FileNode;

public class ResourceMapper {

    public static ResourceDto toDto(FileNode node) {
        String name = node.isDirectory() ? node.getName() + "/" : node.getName();

        return ResourceDto.builder()
                .path(buildPath(node.getParent()))
                .name(name)
                .size(node.isDirectory() ? null : node.getSizeBytes())
                .type(node.isDirectory() ? "DIRECTORY" : "FILE")
                .build();
    }

    private static String buildPath(FileNode parent) {
        if (parent == null) return "";

        StringBuilder path = new StringBuilder();
        FileNode current = parent;
        while (current != null) {
            path.insert(0, current.getName() + "/");
            current = current.getParent();
        }
        return path.toString();
    }
}

