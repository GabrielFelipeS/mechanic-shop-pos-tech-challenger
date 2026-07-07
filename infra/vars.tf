variable "projectName" {
  default = "mechanic-shop-pos-tech-challenger"
}

variable "region_default" {
  default = "us-east-1"
}

variable "cidr_vpc" {
  default = "10.0.0.0/16"
}

variable "tags" {
  default = {
    Name        = "mechanic-shop-pos-tech-challenger",
    School      = "FIAP",
    Turma       = "15SOAT",
    Environment = "Production",
    Year        = "2026"
  }
}

variable "instance_type" {
  default = "t3.medium"
}

variable "labRole" {
  default = "arn:aws:iam::371269711873:role/LabRole"
}

variable "principalArn" {
  default = "arn:aws:iam::371269711873:role/voclabs"
}
