package com.cloudbackup.backup.model;

import jakarta.persistence.*;

@Entity
@Table(name = "backups")
public class BackupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourcePath;
    private String containerName;
    private String blobName;
    private String fileHash;
    private String status;
    private java.time.Instant timestamp;

    // Constructors
    public BackupEntity() {}

    public BackupEntity(String sourcePath, String containerName, String blobName, String fileHash, String status, java.time.Instant timestamp) {
        this.sourcePath = sourcePath;
        this.containerName = containerName;
        this.blobName = blobName;
        this.fileHash = fileHash;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }

    public String getContainerName() { return containerName; }
    public void setContainerName(String containerName) { this.containerName = containerName; }

    public String getBlobName() { return blobName; }
    public void setBlobName(String blobName) { this.blobName = blobName; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.Instant getTimestamp() { return timestamp; }
    public void setTimestamp(java.time.Instant timestamp) { this.timestamp = timestamp; }
}
