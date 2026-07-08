resource "aws_s3_bucket" "bucket-backend" {
  bucket = "bucket-${var.projectName}"
}
