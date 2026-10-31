package com.cloudbackup.backup.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {

    private static final Logger logger = LoggerFactory.getLogger(AzureBlobConfig.class);

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    public CommandLineRunner initializeContainer(BlobServiceClient blobServiceClient) {
        return args -> {
            try {
                logger.info("Initializing Azure Blob Storage container: {}", containerName);

                var containerClient = blobServiceClient.getBlobContainerClient(containerName);
                boolean created = containerClient.createIfNotExists();

                if (created) {
                    logger.info("Container '{}' was created", containerName);
                } else {
                    logger.info("Container '{}' already exists", containerName);
                }

            } catch (Exception e) {
                logger.error("Failed to initialize container '{}'. Error: {}", containerName, e.getMessage(), e);
            }
        };
    }
}

