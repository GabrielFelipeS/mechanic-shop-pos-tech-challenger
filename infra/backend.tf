terraform {
  backend "s3" {
    bucket = "mechanic-shop-pos-tech-challenger"
    key    = "backend/terraform.tfstate"
    region = "us-east-1"
  }
}
