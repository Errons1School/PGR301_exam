terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.39.0"
    }
  }
  backend "s3" {
    bucket = "candidate2024-terraform-state"
    key    = "candidate2024-terraform.state"
    region = "eu-west-1"
  }
}
