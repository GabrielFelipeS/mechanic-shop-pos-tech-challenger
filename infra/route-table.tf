resource "aws_route_table" "route_table_public" {
  vpc_id = aws_vpc.vpc_main.id

  route {
    cidr_block = var.cidr_block
    gateway_id = "local"
  }
}

resource "aws_route_table_association" "route_table_public_association" {
  subnet_id      = aws_subnet.subnet_public[*].id
  route_table_id = aws_route_table.route_table_public.id
}