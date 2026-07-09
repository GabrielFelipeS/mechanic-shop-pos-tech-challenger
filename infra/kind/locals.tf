locals {
  namespace_files = fileset("${path.module}/manifests/00-namespaces", "*.yaml")
  config_files    = fileset("${path.module}/manifests/01-config", "*.yaml")
  app_files       = fileset("${path.module}/manifests/02-app", "*.yaml")
}