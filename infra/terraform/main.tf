terraform {
  required_version = ">= 1.5"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
  # TODO: Configure remote state backend
  # backend "gcs" {
  #   bucket = "your-terraform-state-bucket"
  #   prefix = "appstarterkit"
  # }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

module "cloud_sql" {
  source     = "./modules/cloud-sql"
  project_id = var.project_id
  region     = var.region
  env        = var.env
}

module "redis" {
  source     = "./modules/redis"
  project_id = var.project_id
  region     = var.region
  env        = var.env
}

module "cloud_run" {
  source       = "./modules/cloud-run"
  project_id   = var.project_id
  region       = var.region
  env          = var.env
  image        = var.backend_image
  database_url = module.cloud_sql.database_url
  redis_url    = module.redis.redis_url
}
