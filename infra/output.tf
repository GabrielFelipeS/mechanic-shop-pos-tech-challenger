# VPC Outputs
output "vpc_cidr_block" {
  value = aws_vpc.vpc_main.cidr_block
}
output "vpc_cidr_id" {
  value = aws_vpc.vpc_main.id
}

# Subnet Outputs
output "subnet_cidr_block" {
  value = aws_subnet.subnet_public[*].cidr_block
}
output "subnet_id" {
  value = aws_subnet.subnet_public[*].id
}