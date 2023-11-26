# Eksamen PGR301 DevOps i skyen

## Besvarelse av kandidat 2014

### Lise av GitHub Secrets benyttet:

| Github Secrets        | Description                                         | Eksempel                                                              |
|-----------------------|-----------------------------------------------------|-----------------------------------------------------------------------|
| AWS_ACCESS_KEY_ID     | AWS access key for aws cli.                         | \* AKIAIOSFODNN7EXAMPLE                                               |
| AWS_SECRET_ACCESS_KEY | AWS secret access key for aws cli.                  | \* wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY                           |
| AWS_REGION            | Region for deployment of resources.                 | eu-west-1                                                             |
| AWS_ECR_URI           | Base URI for ECR repository.                        | ***account***.dkr.ecr.***region***.amazonaws.com/***repositoryName*** |
| AWS_ALARM_EMAIL       | Regular email for sending alarms notifications too. | name@domain.no                                                        |

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
| prefix           |    no    |     -     | A common name in front of every resource in AWS terraform makes.                                     | candidate2014                                                               |
| region           |   yes    | eu-west-1 | Region for deployment of resources.                                                                  | eu-west-1                                                                   |
| apprunner_image  |    no    |     -     | AWS ECR name for image for apprunner to run.                                                         | ***account***.dkr.ecr.***region***.amazonaws.com/***repository***:***tag*** |
| apprunner_cpu    |   yes    |    256    | Valid values: 256\|512\|1024\|2048\|4096\|(0.25\|0.5\|1\|2\|4) vCPU.                                 | 256                                                                         |
| apprunner_memory |   yes    |   1024    | Valid values: 512\|1024\2048\|3072\|4096\|6144\|8192\|10240\|12288\|(0.5\|12\|3\4\|6\|8\|10\|12) GB. | 1024                                                                        |                                                     |         |
| email            |    no    |     -     | Regular email for sending alarms notifications too.                                                  | name@domain.no                                                              |


## Oppgave 3B:
Oppgaven er løst og beskrivelse på hva som trengs for at den skal kjøre ligger i oppgave 2B.

## Oppgave 4A
I koden min har jeg benyttet Meter og Time. I kontrolleren har jeg tre endepunkter hvor en er den originale i oppgaven 
og de to andre er selv laget. I den originale samles det tre Meter datapunkter; 
en er hvor mange ganger en fullfører PPE-scan tjeneste, 
andre er hvor mange PPE-scan som blir stemplet som å ikke bruke PPE utstyr og 
tredje er hvor mange ganger den får en HTTP 500 Internal Server Error.

For de selvlagde tjenestene så benyttes det to Meter datapunkter; 
en er hvor mange ganger det blir gjort en fullført TEXT-scan og 
andre er hvor mange ganger den får en HTTP 500 Internal Server Error.

Alle tre punktene benytter Time for å måle gjennomsnittlig responstid og maks responstid.
Hvorfor alle punktene måler fullført, og HTTP 500 feil er følgende:
- Siden Spring måler hvor mange som kommer inn, men ved starten av endepunktene er det kriterier som møtes 
som ikke gir HTTP 500 eller HTTP 200. Dette kan benyttes for å sjekke om DDOS eller 
indikere om at ekstern dokumentasjon på endepunkt må forbedres.
- Siden denne serveren basere seg på eksterne tjenester og måler om oppgaven feiles er det viktig å måle feilede forsøk
med å benytte AWS Rekognitoin. Hvis den overstiger for høy tall vill det aktivere en alarm og sende det til kontakt ansvarlig.  

Siden vi benytter meget tids intense tjenester er det viktig å sjekke hvor lang tid hvert endepunkt benytter å fullføre jobben.
Her er samspill mellom maks og gjennomsnittsverdi viktig. Hvis dem ligger tett sammen kjører tjenesten stabilt, 
men har man høye spikes kan dette være tegn på ting som kan forbedres og kan løses med å sjekke log. 
Eller et tegn på at tjenesten er på vei til å gå gale.

## Oppgave 4B
Her er tabellen som forklare hvordan alarm modulen fungerer:

| Name                | Optional | Default                                                                    | Description                                                                      | Example                   |
|:--------------------|:--------:|:---------------------------------------------------------------------------|----------------------------------------------------------------------------------|---------------------------|
| prefix              |   yes    | prefix                                                                     | A common name in front of every resource in AWS terraform makes.                 | candidate2014             |
| alarm_name          |   yes    | alarm-name                                                                 | Name of the alarm.                                                               | To-many-fails             |
| alarm_namespace     |    no    | -                                                                          | Name of the AWS cloudwatch domain to connect the alarm to.                       | candidate2014-dashboard   |
| metric_name         |    no    | -                                                                          | Name of the cloudwatch metric that can trigger the alarm.                        | data_something.count      |
| comparison_operator |   yes    | GreaterThanThreshold                                                       | What condition to trigger the alarm.                                             | GreaterThanThreshold      |
| threshold           |   yes    | 10                                                                         | Value used for comparison_operator.                                              | 1000                      |
| evaluation_periods  |   yes    | 2                                                                          | How many periods the threshold will be using as valid data to trigger the alarm. | 4                         |
| period              |   yes    | 60                                                                         | How long a period is in seconds.                                                 | 120                       |
| statistic           |   yes    | Sum                                                                        | How to format the metric_name to compare with the threshold.                     | Max                       |
| alarm_description   |   yes    | This alarm goes off when data goes over 10values 2 times within 60 seconds | Description of the alarm.                                                        | This alarm does something |
| aws_sns_topic_name  |   yes    | alarm-topic                                                                | Name for the AWS sns resource.                                                   | alarm-topic               |
| protocol            |    no    | -                                                                          | What protocol will be used, like email or sms.                                   | tlf                       |
| endpoint            |    no    | -                                                                          | What email or phone number to contact when alarm goes off.                       | +4711223344               |


## Oppgave 5A
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

## Oppgave 5B-1
SCRUM i korte trekk er et rammeverk i prosjektledelse og er ofte brukt rundt programvareutvikling. 
Hovedfokuset er samarbeid innad i teamet og iterative sykluser for utvikling. Disse syklusene kan være alt fra 1-4 uker lange. 
En annen viktig del er at prosjektet skal deles up i mange små oppgaver som skal være håndterbart for rask gjennomføring
og kan gjøres mellom en til få personer kan gjøre.

Det som fort kan bli utfordrerne med SCRUM er hvis utviklings teamet blir for løst til syklusene på 1-4 uker.
Hvis syklusen blir for stor kan den smidigheten man originalt ønsket forsvinne. Når alt skal stykkes opp og
planlegger “mikro” detaljer kan man bli for lite smidig. 
Skulle en større hendelse skje vill man trolig ikke å klare å tilpasse seg. 
Ei annen nedside bak SCRUM er dens store avhengighet av møter. Hvis det blir for mye byråkrati rundt det å utvikle.


## Oppgave 5B-2
DevOps søker å samkjøre prosessene mellom programvareutvikling (Dev) og IT-operasjoner (Ops). 
De grunnleggende prinsipper og praksiser fokuserer på automatisering, kontinuerlig integrering og levering (CI/CD),
tett samarbeid, hurtig tilbakemelding, og konstant forbedring. 
DevOps integrerer utvikling og drift ved å bygge kultur der disse jobber sammen gjennom hele applikasjonens livssyklus.

Bruken av DevOps har en betydelig positiv effekt på både kvaliteten og leveransetempoet i programvareutvikling. 
Med automatisering av repetitive oppgaver og standardisering av utviklings prosesser, reduseres menneskelige feil, og
software blir levert pålitelig. Samtidig driver DevOps med kontinuerlig tilbakemelding, hvor problemer identifiseres og løses raskt.
Her kan man se direkte til lean modellen som bilfabrikker i Japan fant opp.

Utfordringer med DevOps kan være kulturendring, som kan være vanskelig i organisasjoner som er vant til tradisjonelle metoder.
Kompleksiteten i å administrere DevOps verktøy kan også være en utfordring, spesielt for team uten tilstrekkelig opplæring eller erfaring. 
I tillegg kan for stort fokus på automatisering og rask levering noen ganger kompromittere kvaliteten.


## Oppgave 5B-3
Scrum og DevOps er begge sentrale metodikker i programvareutvikling, 
men de har ulike fokusområder og påvirker programvarekvalitet og leveransetempo på forskjellige måter.

Scrum legger stor vekt på fleksibilitet, teamarbeid og iterativ utvikling. Hvor fokus er kontinuerlig forbedring, 
tilpasning til endrede krav, og hyppig leveranse av nye features. Scrum er spesielt nyttig i prosjekter hvor 
kravene er forventet å endre seg eller er ikke fullstendig definerte fra starten.

DevOps fokuserer på å forene programvareutvikling og IT-operasjoner, med et sterkt fokus på automatisering og CI/CD. 
Dette fører til raskere leveransetempo, ettersom endringer raskt kan integreres og distribueres til produksjonsmiljøet. 
Denne tilnærmingen er spesielt gunstig i miljøer der rask leveranse og høy tilgjengelighet er i fokus.

Scrum og DevOps er ikke nødvendigvis eksklusive og kan kombineres. Scrum kan håndtere den menneskelige siden av programvareutviklingen, 
mens DevOps fokuserer på operasjonelle aspekter og automatisering. Sammen kan de tilby en omfattende tilnærming til programvareutvikling.

## Oppgave 5C
Ved ny funksjonalitet i en app er lagt til er det flere metrikere som kan benyttes. 
For teknisk analyse er det viktig å sjekke data som oppetid, responstider og tilsvarende metriker for stabilitet. 
Avhengig av applikasjonen kan det være veldig relevant å hente data som sjekker om mengden inntekter har økt eller ikke. 
Dette kan fort gi tilbakemelding på brukeren sin opplevelse uten å nødvendig hvis si hva feilen er.

Når man har tilstrekkelig metrisk av applikasjonen, kan man bygge hypoteser fra trender og forbedre på det. 
Siden denne dataen forklare oss utviklere hva problemet danner dette en positiv feedback loop.

Integrering av feedback i ulike stadier av utviklingslivssyklusen, fra tidlig planlegging og utvikling til utgivelse og 
videre iterasjoner, bidrar feedback til å veilede beslutninger. Ved å bruke både kvantitative data og kvalitative data 
(som brukeranmeldelser og tilbakemeldinger), kan utviklingsteamet oppnå en forståelse av hvordan applikasjonens generelle ytelse. 