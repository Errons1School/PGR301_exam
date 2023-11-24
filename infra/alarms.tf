module "alarm-a" {
  source              = "github.com/Errons1/aws_alarm.git"
  prefix              = var.prefix
  alarm_namespace     = aws_cloudwatch_dashboard.main.dashboard_name
  alarm_name          = "To many failed PPE scan request"
  metric_name         = "crash_scan_ppe.count"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "10"
  evaluation_periods  = "2"
  period              = "60"
  statistic           = "Sum"
  alarm_description   = "To many failed PPE scan request"
  protocol            = "email"
  endpoint            = var.email
}

module "alarm-b"  {
  source              = "github.com/Errons1/aws_alarm.git"
  prefix              = var.prefix
  alarm_namespace     = aws_cloudwatch_dashboard.main.dashboard_name
  alarm_name          = "To many failed text scan request"
  metric_name         = "crash_scan_text.count"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "10"
  evaluation_periods  = "2"
  period              = "60"
  statistic           = "Sum"
  alarm_description   = "To many failed text scan request"
  protocol            = "email"
  endpoint            = var.email
}

module "alarm-c" {
  source              = "github.com/Errons1/aws_alarm.git"
  prefix              = var.prefix
  alarm_namespace     = aws_cloudwatch_dashboard.main.dashboard_name
  alarm_name          = "To many failed backup text scan request"
  metric_name         = "total_scan_text_backup.count"
  comparison_operator = "GreaterThanThreshold"
  threshold           = "10"
  evaluation_periods  = "2"
  period              = "60"
  statistic           = "Sum"
  alarm_description   = "To many failed backup text scan request"
  protocol            = "email"
  endpoint            = var.email
}