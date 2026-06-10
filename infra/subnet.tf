resource "aws_subnet" "subnet_public" {
  count = 3

  vpc_id                  = aws_vpc.vpc_main.id
  cidr_block              = cidrsubnet(aws_vpc.vpc_main.cidr_block, 4, count.index)
  map_public_ip_on_launch = true
  availability_zone       = ["sa-east-1a", "sa-east-1b", "sa-east-1c"][count.index]
}