package org.cloudstorage.repository;

import org.cloudstorage.model.entity.FileNode;
import org.cloudstorage.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;



public interface FileNodeRepository extends JpaRepository<FileNode, Long> {
    Optional<FileNode> findByOwnerAndParentAndName(User owner, FileNode parent, String name);
    List<FileNode> findByOwnerAndParent(User owner, FileNode parent);
    List<FileNode> findAllByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name);
}
