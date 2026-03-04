resource "google_sql_database_instance" "postgres" {
  name             = "appstarterkit-${var.env}"
  database_version = "POSTGRES_16"
  region           = var.region
  project          = var.project_id

  # Prevent accidental deletion
  deletion_protection = var.env == "production"

  settings {
    tier              = var.env == "production" ? "db-n1-standard-2" : "db-f1-micro"
    availability_type = var.env == "production" ? "REGIONAL" : "ZONAL"
    disk_autoresize   = true
    disk_size         = var.env == "production" ? 50 : 10
    disk_type         = "PD_SSD"

    backup_configuration {
      enabled                        = true
      start_time                     = "02:00"
      point_in_time_recovery_enabled = var.env == "production"
      transaction_log_retention_days = var.env == "production" ? 7 : 1
      backup_retention_settings {
        retained_backups = var.env == "production" ? 14 : 3
        retention_unit   = "COUNT"
      }
    }

    maintenance_window {
      day          = 7  # Sunday
      hour         = 3
      update_track = "stable"
    }

    ip_configuration {
      ipv4_enabled    = false
      private_network = var.vpc_network != "" ? var.vpc_network : null
      require_ssl     = true
    }

    database_flags {
      name  = "max_connections"
      value = var.env == "production" ? "200" : "50"
    }
  }
}

resource "google_sql_database" "app" {
  name     = "appstarterkit_${var.env}"
  instance = google_sql_database_instance.postgres.name
  project  = var.project_id
}

resource "google_sql_user" "app" {
  name     = "appstarterkit"
  instance = google_sql_database_instance.postgres.name
  password = var.db_password
  project  = var.project_id
}
