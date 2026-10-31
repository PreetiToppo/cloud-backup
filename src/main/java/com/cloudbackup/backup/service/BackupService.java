package com.cloudbackup.backup.service;

import com.cloudbackup.backup.model.BackupEntity;

public interface BackupService {

    BackupEntity performBackup(String sourcePath);

    void restoreFile(String blobName, String destinationPath);
}
