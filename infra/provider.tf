terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.25.0"
    }
  }
  backend "s3" {
    bucket = "candidate2014-terraform-state"
    key    = "candidate2014-terraform.state"
    region = "eu-west-1"
  }
}
