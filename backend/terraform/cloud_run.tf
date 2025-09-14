# This Terraform file defines the Google Cloud Run services.
# It will contain resource blocks for:
# - The 'bff' service, configuring its container image, memory, and environment variables.
# - The 'api-proxy' service, with similar configurations.
#
# This allows the services to be deployed and updated in a repeatable and version-controlled manner.
#
# Depends on:
# - Container images being available in Google Container Registry (GCR) or Artifact Registry.
# - `variables.tf` for project ID, region, etc.
