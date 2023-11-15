terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.25.0"
    }
  }
  backend "s3" {
    bucket = "candidate2024-terraform-state"
    key    = "candidate2024-terraform.state"
    region = "eu-west-1"
  }
}
