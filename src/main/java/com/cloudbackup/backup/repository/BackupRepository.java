package com.cloudbackup.backup.repository;

import com.cloudbackup.backup.model.BackupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupRepository extends JpaRepository<BackupEntity, Long> {

    BackupEntity findByFileHash(String fileHash);
}
