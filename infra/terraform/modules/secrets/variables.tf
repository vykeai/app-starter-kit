variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "env" {
  description = "Environment (staging, production)"
  type        = string
}

variable "database_url" {
  description = "PostgreSQL connection string to store as secret"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT signing secret to store as secret"
  type        = string
  sensitive   = true
}

variable "redis_url" {
  description = "Redis connection string to store as secret"
  type        = string
  sensitive   = true
}

variable "cloud_run_sa_email" {
  description = "Cloud Run service account email to grant secret access"
  type        = string
}
