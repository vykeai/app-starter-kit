# AppStarterKit Infrastructure (GCP)

Terraform modules for deploying to Google Cloud Platform.

## Prerequisites
- Terraform >= 1.5
- gcloud CLI authenticated
- GCP project created

## Structure
- `modules/cloud-run/` — Backend API on Cloud Run
- `modules/cloud-sql/` — PostgreSQL on Cloud SQL
- `modules/redis/` — Redis on Memorystore
- `modules/secrets/` — Secret Manager for env vars

## Usage
```bash
cd infra/terraform
terraform init
terraform workspace new staging
terraform plan -var-file=envs/staging.tfvars
terraform apply -var-file=envs/staging.tfvars
```
