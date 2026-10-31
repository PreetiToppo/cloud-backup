# Cloud-Native Backup & Storage System

A scalable, distributed backup service built with Java and Spring Boot that leverages Azure Blob Storage for secure, deduplicated cloud data protection. 

## 🚀 Features
*   **Cloud Storage Integration:** Uploads and restores files using Azure Blob Storage (SDK v12).
*   **Deduplication:** Computes SHA-256 hashes of files to prevent redundant uploads, saving cloud storage costs and bandwidth.
*   **Database Persistence:** Tracks backup metadata (source path, hash, blob name, status) using Spring Data JPA and an H2 database.
*   **Monitoring:** Exposes custom Micrometer metrics (`backup.success`, `backup.failure`) via Prometheus for observability.
*   **Containerization:** Fully Dockerized using multi-stage builds for minimal production images.
*   **Orchestration:** Includes Kubernetes manifests (Deployments, Services, ConfigMaps) for scalable deployment.

## 🛠️ Tech Stack
*   **Language:** Java 17
*   **Framework:** Spring Boot 3 (Web, Data JPA, Actuator)
*   **Cloud:** Azure Blob Storage
*   **DevOps:** Docker, Kubernetes, Prometheus
*   **Testing:** JUnit 5, Mockito, Azurite (Azure Storage Emulator)

## ⚙️ How to Run Locally

### Prerequisites
*   Java 17
*   Maven
*   Node.js (for Azurite emulator)

### 1. Start the Azure Storage Emulator (Azurite)
Since this project uses Azure Blob Storage, we use Azurite for local testing. Run the following command in your terminal:
```bash
npm install -g azurite
azurite --silent --location ./azurite-data
