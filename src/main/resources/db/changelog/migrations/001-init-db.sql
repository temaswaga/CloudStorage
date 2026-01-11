--liquibase formatted sql

--changeset yourname:1
CREATE TABLE users (
                       id BINARY(16) PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE
);

--changeset yourname:2
CREATE TABLE file_nodes (
                            id BINARY(16) PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            is_directory BOOLEAN NOT NULL DEFAULT false,
                            parent_id BINARY(16),
                            owner_id BINARY(16) NOT NULL,
                            size_bytes BIGINT DEFAULT 0,
                            s3_key VARCHAR(512),
                            CONSTRAINT fk_file_parent FOREIGN KEY (parent_id) REFERENCES file_nodes(id) ON DELETE CASCADE,
                            CONSTRAINT fk_file_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

--changeset yourname:3
CREATE INDEX idx_file_nodes_owner_parent ON file_nodes(owner_id, parent_id);