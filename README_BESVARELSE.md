# Eksamen PGR301 DevOps i skyen
## Besvarelse fra kandidat 2014
### Lise av GitHub Secrets benyttet:
| Github Secrets        | Description                                         | Eksempel                                         |
|-----------------------|-----------------------------------------------------|--------------------------------------------------|
| AWS_ACCESS_KEY_ID     | AWS access key for aws cli.                         | \* AKIAIOSFODNN7EXAMPLE                          |
| AWS_SECRET_ACCESS_KEY | AWS secret access key for aws cli.                  | \* wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY      |
| AWS_REGION            | Region for deployment of resources.                 | eu-west-1                                        |
| AWS_ECR_URI           | Base URI for ECR repository.                        | ***account***.dkr.ecr.***region***.amazonaws.com |
| AWS_ALARM_EMAIL       | Regular email for sending alarms notifications too. | name@domain.no                                   |


*Note: these are example keys collected from: [Managing access keys for IAM users](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html)





Part 1:
- added "AmazonS3FullAccess" to template.yaml

sam build --use-container
sam deploy --no-confirm-changeset --no-fail-on-empty-changeset --s3-bucket ${{ env.AWS_SAM_S3_ARTIFACT }}

explain github secrets 
explain AWS_SAM_S3_ARTIFACT: pgr301-sam-bucket aka where the artifact saves it 

      Events:
        RekognitionProtectiveWear:
          Type: Api # More info about API Event Source: https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#api
          Properties:
            Path: /
            Method: get

path is on purpose / for the URL to directly work from sam cli

Part 2:
note hardcoded regions in code!


