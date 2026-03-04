output "service_url" {
  description = "Cloud Run service URL"
  value       = google_cloud_run_v2_service.backend.uri
}

output "service_account_email" {
  description = "Service account email used by Cloud Run"
  value       = google_service_account.cloud_run.email
}
