package com.cloudbackup.backup.service;

import com.azure.core.exception.ResourceExistsException;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.cloudbackup.backup.model.BackupEntity;
import com.cloudbackup.backup.repository.BackupRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
public class BackupServiceImpl implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupServiceImpl.class);

    private final BlobServiceClient blobServiceClient;
    private final BackupRepository backupRepository;
    private final MeterRegistry meterRegistry;
    private final String containerNameValue;

    public BackupServiceImpl(BlobServiceClient blobServiceClient,
                             BackupRepository backupRepository,
                             MeterRegistry meterRegistry,
                             @Value("${azure.storage.container-name}") String containerNameValue) {
        this.blobServiceClient = blobServiceClient;
        this.backupRepository = backupRepository;
        this.meterRegistry = meterRegistry;
        this.containerNameValue = containerNameValue;
    }

    @Override
    public BackupEntity performBackup(String sourcePath) {
        Counter successCounter = meterRegistry.counter("backup.success");
        Counter failureCounter = meterRegistry.counter("backup.failure");

        try {
            Path filePath = Paths.get(sourcePath);
            if (!Files.exists(filePath)) {
                throw new FileNotFoundException("Source file not found: " + sourcePath);
            }

            // Compute SHA-256 hash
            String fileHash = computeSha256(filePath);
            String originalFileName = filePath.getFileName().toString();

            // Check for duplicate
            BackupEntity existing = backupRepository.findByFileHash(fileHash);
            if (existing != null) {
                logger.info("Duplicate detected - file already backed up with hash: {}", fileHash);
                return existing;
            }

            // Upload to Azure Blob
            String blobName = fileHash + "-" + originalFileName;
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerNameValue);
            try {
                containerClient.getBlobClient(blobName).uploadFromFile(filePath.toString(), true);
            } catch (ResourceExistsException e) {
                logger.warn("Blob already exists in storage, skipping upload for blobName: {}", blobName);
            }

            // Save to database
            BackupEntity backupEntity = new BackupEntity();
            backupEntity.setSourcePath(sourcePath);
            backupEntity.setContainerName(containerNameValue);
            backupEntity.setBlobName(blobName);
            backupEntity.setFileHash(fileHash);
            backupEntity.setStatus("COMPLETED");
            backupEntity.setTimestamp(Instant.now());

            BackupEntity saved = backupRepository.save(backupEntity);
            successCounter.increment();

            logger.info("Backup completed successfully: {}", blobName);
            return saved;

        } catch (Exception e) {
            failureCounter.increment();
            logger.error("Backup failed for source path: {}", sourcePath, e);
            throw new RuntimeException("Backup operation failed", e);
        }
    }

    @Override
    public void restoreFile(String blobName, String destinationPath) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerNameValue);
            containerClient.getBlobClient(blobName).downloadToFile(destinationPath, true);

            logger.info("File restored successfully from blob: {} to destination: {}", blobName, destinationPath);

        } catch (Exception e) {
            logger.error("Restore failed for blob: {} to destination: {}", blobName, destinationPath, e);
            throw new RuntimeException("Restore operation failed", e);
        }
    }

    private String computeSha256(Path filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
