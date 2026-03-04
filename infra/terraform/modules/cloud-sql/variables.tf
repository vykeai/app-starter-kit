variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "region" {
  description = "GCP region"
  type        = string
}

variable "env" {
  description = "Environment (staging, production)"
  type        = string
}

variable "db_password" {
  description = "Database user password"
  type        = string
  sensitive   = true
  default     = ""  # TODO: set via Secret Manager or tfvars
}

variable "vpc_network" {
  description = "VPC network self-link for private IP (leave empty for public IP)"
  type        = string
  default     = ""
}
