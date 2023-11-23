resource "aws_apprunner_service" "service" {
  service_name = "${ var.prefix }-apprunner"

  instance_configuration {
    instance_role_arn = aws_iam_role.role_for_apprunner_service.arn
    cpu               = var.apprunner_cpu
    memory            = var.apprunner_memory
  }

  source_configuration {
    authentication_configuration {
      access_role_arn = "arn:aws:iam::244530008913:role/service-role/AppRunnerECRAccessRole"
    }
    image_repository {
      image_configuration {
        port = "8080"
      }
      image_identifier      = var.apprunner_image
      image_repository_type = "ECR"
    }
    auto_deployments_enabled = true
  }
}

resource "aws_iam_role" "role_for_apprunner_service" {
  name               = "${ var.prefix }-apprunner-role"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}


data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["tasks.apprunner.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "policy" {
  statement {
    effect    = "Allow"
    actions   = ["rekognition:*"]
    resources = ["*"]
  }

  statement {
    effect    = "Allow"
    actions   = ["s3:*"]
    resources = ["*"]
  }

  statement {
    effect    = "Allow"
    actions   = ["*:*"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "policy" {
  name        = "${ var.prefix }-apprunner-policy"
  description = "Policy for apprunner"
  policy      = data.aws_iam_policy_document.policy.json
}


resource "aws_iam_role_policy_attachment" "attachment" {
  role       = aws_iam_role.role_for_apprunner_service.name
  policy_arn = aws_iam_policy.policy.arn
}