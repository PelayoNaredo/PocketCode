# This Terraform file defines the Cloud Firestore database configuration.
# It will contain resource blocks to:
# - Enable the Firestore API.
# - Define the database instance itself (e.g., in Native mode).
# - Set up security rules for the collections (`users`, `projects`, `builds`).
# - Potentially define indexes required for complex queries.
#
# This ensures the database schema and rules are managed as code.
#
# Depends on:
# - `variables.tf` for project ID and region.
