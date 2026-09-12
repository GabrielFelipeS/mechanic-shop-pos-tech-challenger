# ---------------------------------------------------------------------------
# Rede
#
# VPC, subnets publicas, internet gateway e route table sao recursos liberados
# no AWS Academy. Todas as subnets sao publicas com auto-assign de IP publico,
# para que os nodes recebam IP externo e os NodePorts fiquem acessiveis sem
# precisar de LoadBalancer (que o lab nem sempre permite criar).
# ---------------------------------------------------------------------------

data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_vpc" "vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "vpc-${var.project_name}"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id

  tags = {
    Name = "igw-${var.project_name}"
  }
}

resource "aws_subnet" "public" {
  count = var.subnet_count

  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = cidrsubnet(aws_vpc.vpc.cidr_block, 4, count.index)
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name                     = "subnet-public-${count.index}-${var.project_name}"
    "kubernetes.io/role/elb" = "1"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name = "rt-public-${var.project_name}"
  }
}

resource "aws_route_table_association" "public" {
  count = var.subnet_count

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# ---------------------------------------------------------------------------
# Acesso externo aos NodePorts
#
# Um managed node group sem launch template usa o security group que o proprio
# EKS cria para o cluster. Abrir portas em um security group avulso nao teria
# efeito nenhum nos nodes; por isso a regra e adicionada direto no
# cluster_security_group_id.
# ---------------------------------------------------------------------------

resource "aws_vpc_security_group_ingress_rule" "nodeport" {
  security_group_id = aws_eks_cluster.cluster.vpc_config[0].cluster_security_group_id
  description       = "Acesso externo a faixa de NodePort (API e Mailpit)"

  cidr_ipv4   = var.nodeport_allowed_cidr
  ip_protocol = "tcp"
  from_port   = 30000
  to_port     = 32767
}
