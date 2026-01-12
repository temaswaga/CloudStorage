package org.cloudstorage.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "file_nodes")
public class FileNode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(java.sql.Types.BINARY)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_directory", nullable = false)
    private Boolean isDirectory = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JdbcTypeCode(java.sql.Types.BINARY)
    private FileNode parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JdbcTypeCode(java.sql.Types.BINARY)
    private User owner;

    @Column(name = "size_bytes")
    private Long sizeBytes = 0L;

    @Column(name = "s3_key", length = 512)
    private String s3Key;
}