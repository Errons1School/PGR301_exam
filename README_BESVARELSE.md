2014



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


