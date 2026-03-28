package org.cloudstorage.mapper;

import org.cloudstorage.model.dto.ResourceDto;
import org.cloudstorage.model.entity.FileNode;

public class ResourceMapper {

    public static ResourceDto toDto(FileNode node) {
        return ResourceDto.builder()
                .path(buildPath(node.getParent()))
                .name(node.getName())
                .size(node.isDirectory() ? null : node.getSizeBytes())
                .type(node.isDirectory() ? "DIRECTORY" : "FILE")
                .build();
    }

    private static String buildPath(FileNode parent) {
        if (parent == null) {
            return "/";
        }
        StringBuilder path = new StringBuilder();
        FileNode current = parent;
        while (current != null) {
            path.insert(0, "/" + current.getName());
            current = current.getParent();
        }
        return path + "/";
    }
}

