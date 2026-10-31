package com.cloudbackup.backup.controller;

import com.cloudbackup.backup.model.BackupEntity;
import com.cloudbackup.backup.repository.BackupRepository;
import com.cloudbackup.backup.service.BackupService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/backups")
public class BackupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);

    private final BackupService backupService;
    private final BackupRepository backupRepository;
    private final MeterRegistry meterRegistry;

    public BackupController(BackupService backupService, BackupRepository backupRepository, MeterRegistry meterRegistry) {
        this.backupService = backupService;
        this.backupRepository = backupRepository;
        this.meterRegistry = meterRegistry;
    }

    // Request DTOs
    public record BackupRequest(@NotBlank String sourcePath) {}

    public record RestoreRequest(@NotBlank String blobName, @NotBlank String destinationPath) {}

    public record BackupResponse(Long id, String fileName, String fileHash, String status, Instant timestamp) {}

    // POST / - Perform backup
    @PostMapping
    public ResponseEntity<BackupEntity> backup(@Valid @RequestBody BackupRequest request) {
        logger.info("Incoming backup request for sourcePath: {}", request.sourcePath());

        BackupEntity result = backupService.performBackup(request.sourcePath());

        logger.info("Backup completed for sourcePath: {} with blobName: {}", request.sourcePath(), result.getBlobName());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // GET /{id} - Get backup by ID
    @GetMapping("/{id}")
    public ResponseEntity<BackupEntity> getBackup(@PathVariable Long id) {
        logger.info("Incoming get backup request for id: {}", id);

        Optional<BackupEntity> backup = backupRepository.findById(id);
        if (backup.isEmpty()) {
            logger.warn("Backup not found for id: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Backup not found");
        }

        return ResponseEntity.ok(backup.get());
    }

    // POST /restore - Restore file
    @PostMapping("/restore")
    public ResponseEntity<Map<String, String>> restore(@Valid @RequestBody RestoreRequest request) {
        logger.info("Incoming restore request for blobName: {} to destination: {}", 
                request.blobName(), request.destinationPath());

        backupService.restoreFile(request.blobName(), request.destinationPath());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Restore successful");

        logger.info("Restore completed for blobName: {}", request.blobName());

        return ResponseEntity.ok(response);
    }
}
