variable "prefix" {
  type    = string
  default = "candidate2014"
}

variable "region" {
  type    = string
  default = "eu-west-1"
}

variable "apprunner_image" {
  type    = string
  default = "244530008913.dkr.ecr.eu-west-1.amazonaws.com/candidate2014:latest"
}

variable "apprunner_cpu" {
  type    = string
  default = "256"
}

variable "apprunner_memory" {
  type    = string
  default = "1024"
}