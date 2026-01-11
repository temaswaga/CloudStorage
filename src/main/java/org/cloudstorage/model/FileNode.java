package org.cloudstorage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "file_nodes")
public class FileNode {
    @Id
    @Column(name = "id", columnDefinition = "uuid not null")
    private Object id;

    @Column(name = "name", nullable = false)
    private String name;

    @ColumnDefault("0")
    @Column(name = "is_directory", nullable = false)
    private Boolean isDirectory = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent_id")
    private FileNode parent;

    @ColumnDefault("0")
    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "s3_key", length = 512)
    private String s3Key;

}