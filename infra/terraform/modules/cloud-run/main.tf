resource "google_cloud_run_v2_service" "backend" {
  name     = "appstarterkit-backend-${var.env}"
  location = var.region
  project  = var.project_id

  template {
    service_account = google_service_account.cloud_run.email

    scaling {
      min_instance_count = var.env == "production" ? 1 : 0
      max_instance_count = var.env == "production" ? 20 : 5
    }

    containers {
      image = var.image

      resources {
        limits = {
          cpu    = var.env == "production" ? "2" : "1"
          memory = var.env == "production" ? "1Gi" : "512Mi"
        }
        startup_cpu_boost = true
      }

      env {
        name  = "NODE_ENV"
        value = var.env
      }

      env {
        name = "DATABASE_URL"
        value_source {
          secret_key_ref {
            secret  = "DATABASE_URL_${upper(var.env)}"
            version = "latest"
          }
        }
      }

      env {
        name = "JWT_SECRET"
        value_source {
          secret_key_ref {
            secret  = "JWT_SECRET"
            version = "latest"
          }
        }
      }

      env {
        name = "REDIS_URL"
        value_source {
          secret_key_ref {
            secret  = "REDIS_URL_${upper(var.env)}"
            version = "latest"
          }
        }
      }

      ports {
        container_port = 3000
      }

      startup_probe {
        http_get {
          path = "/health"
          port = 3000
        }
        initial_delay_seconds = 10
        period_seconds        = 5
        failure_threshold     = 6
      }

      liveness_probe {
        http_get {
          path = "/health"
          port = 3000
        }
        period_seconds    = 30
        failure_threshold = 3
      }
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }
}

resource "google_service_account" "cloud_run" {
  account_id   = "appstarterkit-run-${var.env}"
  display_name = "AppStarterKit Cloud Run (${var.env})"
  project      = var.project_id
}

resource "google_cloud_run_v2_service_iam_member" "public" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.backend.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
