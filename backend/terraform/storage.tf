# This Terraform file defines the Google Cloud Storage buckets.
# It will contain resource blocks for:
# - The `project-source-files` bucket, which stores the raw source code for user projects.
# - The `build-artifacts` bucket, which stores compiled APKs/AABs.
#
# It will also define IAM policies for access control and lifecycle policies
# to manage storage costs by moving old artifacts to cheaper storage classes.
#
# Depends on:
# - `variables.tf` for project ID and location.
