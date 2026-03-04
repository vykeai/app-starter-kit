variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "region" {
  description = "GCP region"
  type        = string
  default     = "us-central1"
}

variable "env" {
  description = "Environment (staging, production)"
  type        = string
}

variable "backend_image" {
  description = "Docker image URL for backend"
  type        = string
}
