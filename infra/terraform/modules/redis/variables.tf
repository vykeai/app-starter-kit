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

variable "reserved_ip_range" {
  description = "CIDR range for Memorystore reserved IP (optional)"
  type        = string
  default     = ""
}
