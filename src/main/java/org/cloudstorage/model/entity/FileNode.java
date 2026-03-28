package org.cloudstorage.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "file_nodes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_file_nodes_owner_parent_name",
                        columnNames = {"owner_id", "parent_id", "name"})
        },
        indexes = {
                @Index(name = "idx_file_nodes_owner_parent", columnList = "owner_id,parent_id")
        }
)
public class FileNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "is_directory", nullable = false)
    private boolean directory = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FileNode parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "size_bytes")
    private Long sizeBytes = 0L;

    @Column(name = "s3_key", length = 512)
    private String s3Key;
}