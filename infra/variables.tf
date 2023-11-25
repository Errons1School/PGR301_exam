variable "prefix" {
  type    = string
}

variable "region" {
  type    = string
  default = "eu-west-1"
}

variable "apprunner_image" {
  type    = string
}

variable "apprunner_cpu" {
  type    = string
  default = "256"
}

variable "apprunner_memory" {
  type    = string
  default = "1024"
}

variable "email" {
  type = string
}