variable "prefix" {
  description = "A prefix to use for naming resources."
  default     = "fiap-mechanic-shop"
}

variable "aws_region" {
  description = "The AWS region to deploy resources in."
  default     = "sa-east-1"
}

variable "cidr_block" {
  description = "The CIDR block for the VPC."
  default     = "10.0.0.0/16"
}
