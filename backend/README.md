# PocketCode IDE - Backend Services

This directory contains all the source code and infrastructure definitions for the backend of the PocketCode IDE.

## Overview

The backend is built on a serverless architecture using Google Cloud Platform (GCP). It consists of two main services, a database, and file storage.

- **/services**: Contains the source code for the microservices.
  - **/bff**: The **Backend for Frontend** service, which is the primary entry point for the mobile client.
  - **/api-proxy**: A **Secure API Proxy** for managing third-party API keys.

- **/terraform**: Contains the **Infrastructure as Code (IaC)** scripts for deploying and managing the GCP resources.

## Deployment

The infrastructure is managed by Terraform. To deploy, navigate to the `terraform` directory and run:
```sh
terraform init
terraform apply
```

The services in the `services` directory are containerized and deployed to Google Cloud Run. Their deployment is managed by the `cloud_run.tf` Terraform file.

## User Project CI/CD

The file at `user-project-template/.github/workflows/android-build.yml` is the template for the remote build pipeline that compiles user projects. This workflow is triggered when a user pushes code to their private project repository.
