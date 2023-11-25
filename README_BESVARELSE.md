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


*Note: these are example keys collected from: [AWS Docs: Managing access keys for IAM users](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html)

## Del 1A:

Kommentar: I denne oppgaven var det ønskelig å bytte navn på mapper og filer, 
men flere steder i oppgave teksten bli original navnene nevnt i sammenheng med terminal kommandoer. 
Derfor har jeg valgt å la de originale navne stå.

For at sensor skal kunne få Action "AWS SAM" til å kjøre, gjør følgende:
I filen `.github/workflows/aws-sam.yaml` må følgende gjøres:
- Det er tre envirment varibler som må settes.
    - **AWS_SAM_S3_ARTIFACT**: Navn på S3 bucket som lager sam artifact.
    - **AWS_BUCKET_NAME**: Navn på S3 bucket som lages av sam som skal inneholde PPE bilder.
    - **AWS_STACK_NAME**: Navn på stacken.

Utenfor prosjektet så må det gjøres to ting:
- I GitHub actions må disse secrets være laget:
    - **AWS_ACCESS_KEY_ID**
    - **AWS_SECRET_ACCESS_KEY**
    - **AWS_REGION**
- I S3 bucket som ble laget av sam, så må det lastes opp noen bilder med PPE utstyr.
Bildene ligger tilgjengelig i mappen `img/ppe/*`.

