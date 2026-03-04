resource "google_redis_instance" "cache" {
  name           = "appstarterkit-redis-${var.env}"
  tier           = "BASIC"
  memory_size_gb = 1
  region         = var.region
  project        = var.project_id

  redis_version     = "REDIS_7_0"
  display_name      = "AppStarterKit Redis (${var.env})"
  reserved_ip_range = var.reserved_ip_range

  auth_enabled            = true
  transit_encryption_mode = "SERVER_AUTHENTICATION"

  maintenance_policy {
    weekly_maintenance_window {
      day = "SUNDAY"
      start_time {
        hours   = 3
        minutes = 0
        seconds = 0
        nanos   = 0
      }
    }
  }
}
