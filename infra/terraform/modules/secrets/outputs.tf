output "database_url_secret_id" {
  description = "Secret Manager secret ID for DATABASE_URL"
  value       = google_secret_manager_secret.database_url.secret_id
}

output "jwt_secret_id" {
  description = "Secret Manager secret ID for JWT_SECRET"
  value       = google_secret_manager_secret.jwt_secret.secret_id
}

output "redis_url_secret_id" {
  description = "Secret Manager secret ID for REDIS_URL"
  value       = google_secret_manager_secret.redis_url.secret_id
}
