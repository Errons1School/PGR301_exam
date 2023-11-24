resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${ var.prefix }-dashboard"
  dashboard_body = jsonencode({
    "widgets" : [
      {
        "type" : "metric"
        "x" : 0
        "y" : 0
        "width" : 12
        "height" : 6

        "properties" : {
          "metrics" : [
            [
              "candidate2014-dashboard", "time_avg_scan_ppe.avg", "exception", "none", "method", "scanForPPE", "class",
              "com.example.s3rekognition.controller.RekognitionController"
            ],
            [".", "time_avg_scan_ppe.max", ".", ".", ".", ".", ".", ".", { "stat" : "Maximum" }]
          ],
          "view" : "gauge",
          "region" : var.region,
          "yAxis" : {
            "left" : {
              "min" : 0,
              "max" : 10000
            }
          },
          "stat" : "Average",
          "period" : 60,
          "title" : "PPE scan avg, PPE scan max"
        }
      },
      {
        "type" : "metric"
        "x" : 12
        "y" : 0
        "width" : 12
        "height" : 6

        "properties" : {
          "metrics" : [
            ["candidate2014-dashboard", "total_scan_ppe_violation.count"],
            [".", "total_scan_ppe.count"],
            [".", "crash_scan_ppe.count"]
          ],
          "view" : "timeSeries",
          "stacked" : false,
          "region" : var.region,
          "stat" : "Sum",
          "period" : 60,
          "title" : "Total violation per PPE scan, Total PPE scan, Total failed PPE scan",
          "liveData" : true
        }
      },
      {
        "type" : "metric"
        "x" : 0
        "y" : 6
        "width" : 12
        "height" : 6

        "properties" : {
          "metrics" : [
            [
              "candidate2014-dashboard", "time_avg_scan_text.avg", "exception", "none", "method", "scanTextOnImage",
              "class", "com.example.s3rekognition.controller.RekognitionController"
            ],
            [".", "time_avg_scan_text.max", ".", ".", ".", ".", ".", ".", { "stat" : "Maximum" }]
          ],
          "view" : "gauge",
          "region" : "eu-west-1",
          "stat" : "Average",
          "period" : 60,
          "yAxis" : {
            "left" : {
              "min" : 0,
              "max" : 5000
            }
          },
          "title" : "Text scan avg, Text scan max"
        }
      },
      {
        "type" : "metric"
        "x" : 12
        "y" : 6
        "width" : 12
        "height" : 6

        "properties" : {
          "metrics" : [
            ["candidate2014-dashboard", "total_scan_text.count"],
            [".", "crash_scan_text.count"]
          ],
          "view" : "timeSeries",
          "stacked" : false,
          "region" : "eu-west-1",
          "stat" : "Sum",
          "period" : 60,
          "title" : "Total text scan, Total failed text scan",
          "liveData" : true
        }
      },
      {
        "type" : "metric"
        "x" : 0
        "y" : 12
        "width" : 12
        "height" : 6

        "properties" : {
          "metrics" : [
            [
              "candidate2014-dashboard", "time_avg_scan_text_backup.avg", "exception", "none", "method",
              "scanTextOnImageBackup", "class", "com.example.s3rekognition.controller.RekognitionController"
            ],
            [
              ".", "time_avg_scan_text_backup.max", ".", ".", ".", ".", ".", ".",
              { "stat" : "Maximum" }
            ]
          ],
          "view" : "gauge",
          "region" : "eu-west-1",
          "stat" : "Average",
          "period" : 60,
          "yAxis" : {
            "left" : {
              "min" : 0,
              "max" : 5000
            }
          },
          "title" : "Text scan backup avg, Text scan backup max"
        }
      },
      {
        "type" : "metric"
        "x" : 12
        "y" : 12
        "width" : 12
        "height" : 6

        "properties" : {
          "metrics" : [
            ["candidate2014-dashboard", "total_scan_text_backup.count"],
            [".", "total_scan_text_backup.count"]
          ],
          "view" : "timeSeries",
          "stacked" : false,
          "region" : "eu-west-1",
          "stat" : "Sum",
          "period" : 60,
          "title" : "Total text backup scan, Total failed text backup scan",
          "liveData" : true
        }
      }
    ]
  })
}