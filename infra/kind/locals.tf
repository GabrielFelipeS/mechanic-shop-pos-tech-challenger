locals {
  namespace_files = fileset("${path.module}/manifests/00-namespaces", "*.yaml")
  config_files    = fileset("${path.module}/manifests/01-config", "*.yaml")
  app_files       = fileset("${path.module}/manifests/02-app", "*.yaml")

  # O Kong le o arquivo declarativo apenas no boot. Este checksum entra como
  # annotation do pod para que uma mudanca de rota provoque rollout.
  kong_config_checksum = sha1(templatefile("${path.module}/manifests/01-config/13-kong-config.yaml", {
    namespace     = var.namespace
    jwt_secret    = var.jwt_secret
    cpf_login_url = var.cpf_login_url
  }))
}
