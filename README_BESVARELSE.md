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

*Note: these are example keys collected
from: [AWS Docs: Managing access keys for IAM users](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html)


## Oppgave 1A:
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


## Oppgave 1B:
Oppgaven er løst og kan utføre instruksjonene fra oppgave teksten.
Kjør følgende fra `/kjell/hello_world`
```shell
docker build -t kjellpy . 
docker run -e AWS_ACCESS_KEY_ID=XXX -e AWS_SECRET_ACCESS_KEY=YYY -e BUCKET_NAME=kjellsimagebucket kjellpy
```


## Oppgave 2A:
Oppgaven er løst og kan løses ifølge instruksjonene fra oppgave teksten.
Kjør følgende fra root
```shell
docker build -t ppe . 
docker run -p 8080:8080 -e AWS_ACCESS_KEY_ID=XXX -e AWS_SECRET_ACCESS_KEY=YYY -e BUCKET_NAME=kjellsimagebucket ppe
```

## Oppgave 2B:
For at sensor skal kunne få Action "AWS SAM" til å kjøre, gjør følgende:
I filen `.github/workflows/aws-ecr.yaml` må følgende gjøres:
- Det er en envirment varibler som må settes.
    - **PREFIX**: Prefix navn som skal settes på alt av AWS resurser.

Utenfor prosjektet så må dette gjøres:
- I GitHub actions må disse secrets være laget (Husk at SAM noen av dem til felles!):
    - **AWS_ACCESS_KEY_ID**
    - **AWS_SECRET_ACCESS_KEY**
    - **AWS_REGION**
    - **AWS_ECR_URI**
    - **AWS_ALARM_EMAIL**

OBS OBS! Inni i prosjektet er det hardkodet en backup løsning for funksjonalitet
i programmet! Den peker til S3 "candidate2014-text".


## Oppgave 3A:
Ting som har blitt gjort med terraform:
- Har lagt inn prefix for alle resurser som lages
- AWS region er en input, men har standard på eu-west-1
- Apprunner krever å vite hvilket image den skal kjøre
- Apprunner CPU er en input, men har standard på 256.
- Apprunner Memory er en inpit, men har en standard på 1024. 
- Trenger email for å sende alarm vaslinger på.

| Name             | Optional |  Default  | Description                                                                                          | Example                                                                     |
|:-----------------|:--------:|:---------:|------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| prefix           |    no    |     -     | A common name in front of every resource in AWS terraform makes                                      | candidate2014                                                               |
| region           |   yes    | eu-west-1 | Region for deployment of resources.                                                                  | eu-west-1                                                                   |
| apprunner_image  |    no    |     -     | AWS ECR name for image for apprunner to run                                                          | ***account***.dkr.ecr.***region***.amazonaws.com/***repository***:***tag*** |
| apprunner_cpu    |   yes    |    256    | Valid values: 256\|512\|1024\|2048\|4096\|(0.25\|0.5\|1\|2\|4) vCPU.                                 | 256                                                                         |
| apprunner_memory |   yes    |   1024    | Valid values: 512\|1024\2048\|3072\|4096\|6144\|8192\|10240\|12288\|(0.5\|12\|3\4\|6\|8\|10\|12) GB. | 1024                                                                        |                                                     |         |
| email            |    no    |     -     | Regular email for sending alarms notifications too.                                                  | name@domain.no                                                              |


## Oppgave 3B:
Oppgaven er løst og beskrivelse på hva som trengs for at den skal kjøre ligger i oppgave 2B.

## Oppgave 4A
Kontinuerlig integrasjon (CI): Det er praktisen for å samle all koden i en hoved produksjons line, 
med andre ord alt som skrives på maskinen til endepunktet til å være kjørende på endestasjon.

CI er i dag en stor del i hvordan man prøver å få til en produksjon av produkter i 
forskjell til hvordan man originalt startet. Tidligere var Fossefall design syklusen den mest utbrette og 
ga lite i form av å være dynamisk og reaktiv. Alt var skrevet og planlagt tidlig og 
ga lite rom for å endre ved hendelser som kunne være katastrofale. 
Hvis et utviklingsteam benytter god CI kultur så skal koden fra den er ferdig implementer ha en rask og 
kort vei igjennom til endestasjonen.

Med CI prøver man å få fram mange fordeler som:
- Raskere og hyppigere utlevering av kode / features.  
- Automatisk testing i flere ledd og nivåer av koden. 
- Raskere responser ved bug og andre sårbarheter i det som er i produksjon.  
- Mindre utfordringer ved utvikling av featerus når det man bygger på oftere testes og er i mindre biter 
- Gir høyere selvtillit til all involvert i produksjonen når koden testen oftere og man har rask reaksjoner ved feil.  
- Mer ytelse for utviklere når mye av det manuelle oppgavene er nå automatisert, som testing, bygging og leveranse til endepunkt.   

I praksis vill dette være ganske fundamentalt hvordan et team jobber, 
i bunnen vill det være et verktøy som assistere å gjøre det mest fundamentale å holde historikk på koden og 
hjelpe med å sy sammen ny kode med eksisterende. Meget utbredt er Git igjennom GitHub benyttet. 
Dette gir utvikleren friheten til å lage en egen “branch” fra hoved koden og utvikle en ny feature fra den.  
Når den er ferdig skal den kunne automatisk bli testet av tester som blir laget samtidig, 
og eldre for å sjekke om systemet ikke brekker. Med verktøyet GitHub, 
blir dette gjort Igjennom det som heter GitHub Actions.

Med et slikt verktøy kan en til flere utvikle på samme feature samtidig og synkronisere enkelt. 
Når en feature er klar vill den samles til hoved koden med en større sjekk og automatiserte tester som er skrevet. 
Hvis alt ser bra ut kan man lage en ny versjon av hoved koden og sende den ut.

Avhenge av hvordan dette struktureres mellom utviklere kan dette gi en rask hastighet ved utvikling til features, 
gir rask muligheter til å fikse koden i hoved basen ved feil og ved feil lager man nye tester som forhindrer lignende.

## Oppgave 4B

## Oppgave 5A

## Oppgave 5B

## Oppgave 5C
