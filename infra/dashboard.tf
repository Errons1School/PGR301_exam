resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${ var.prefix }-dashboard"
  dashboard_body = jsonencode({
    "widgets" : [
      {
        "type" : "metric"
        "x" : 0
        "y" : 0
        "width" : 6
        "height" : 6

        "properties" : {
          "metrics" : [
            ["candidate2014-dashboard", "Text_scan_count.value"]
          ],
          "view" : "gauge",
          "region" : var.region,
          "stat" : "Maximum",
          "period" : 60,
          "yAxis" : {
            "left" : {
              "min" : 0,
              "max" : 10000
            }
          },
          "legend" : {
            "position" : "bottom"
          },
          "title" : "Total text scan"
        }
      },
      {
        "type" : "metric"
        "x" : 6
        "y" : 0
        "width" : 6
        "height" : 6
        
        "properties" : {

          "metrics" : [
            ["candidate2014-dashboard", "PPE_scan_count.value"]
          ],
          "view" : "gauge",
          "region" : var.region,
          "stat" : "Maximum",
          "period" : 60,
          "title" : "Total PPE scan",
          "yAxis" : {
            "left" : {
              "min" : 0,
              "max" : 10000
            }
          }
        }
      },
      {
        "type" : "metric"
        "x" : 0
        "y" : 12
        "width" : 6
        "height" : 6

        "properties" : {
          "metrics": [
            [ "candidate2014-dashboard", "hello_world.count"]
          ],
          "view": "timeSeries",
          "stacked": false,
          "region": var.region,
          "stat": "Sum",
          "period": 60,
          "liveData": true
        }
      }
    ]
  })
}