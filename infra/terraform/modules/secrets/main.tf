locals {
  env_upper = upper(var.env)
}

# ── DATABASE_URL ────────────────────────────────────────────────────────────

resource "google_secret_manager_secret" "database_url" {
  secret_id = "DATABASE_URL_${local.env_upper}"
  project   = var.project_id

  replication {
    auto {}
  }

  labels = {
    env = var.env
    app = "appstarterkit"
  }
}

resource "google_secret_manager_secret_version" "database_url" {
  secret      = google_secret_manager_secret.database_url.id
  secret_data = var.database_url
}

# ── JWT_SECRET ──────────────────────────────────────────────────────────────

resource "google_secret_manager_secret" "jwt_secret" {
  secret_id = "JWT_SECRET"
  project   = var.project_id

  replication {
    auto {}
  }

  labels = {
    app = "appstarterkit"
  }
}

resource "google_secret_manager_secret_version" "jwt_secret" {
  secret      = google_secret_manager_secret.jwt_secret.id
  secret_data = var.jwt_secret
}

# ── REDIS_URL ───────────────────────────────────────────────────────────────

resource "google_secret_manager_secret" "redis_url" {
  secret_id = "REDIS_URL_${local.env_upper}"
  project   = var.project_id

  replication {
    auto {}
  }

  labels = {
    env = var.env
    app = "appstarterkit"
  }
}

resource "google_secret_manager_secret_version" "redis_url" {
  secret      = google_secret_manager_secret.redis_url.id
  secret_data = var.redis_url
}

# ── IAM: grant Cloud Run service account access ─────────────────────────────

resource "google_secret_manager_secret_iam_member" "database_url_accessor" {
  project   = var.project_id
  secret_id = google_secret_manager_secret.database_url.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${var.cloud_run_sa_email}"
}

resource "google_secret_manager_secret_iam_member" "jwt_secret_accessor" {
  project   = var.project_id
  secret_id = google_secret_manager_secret.jwt_secret.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${var.cloud_run_sa_email}"
}

resource "google_secret_manager_secret_iam_member" "redis_url_accessor" {
  project   = var.project_id
  secret_id = google_secret_manager_secret.redis_url.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${var.cloud_run_sa_email}"
}
