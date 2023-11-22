# This is a two step Dockerfile
# First step compiles the java project inside a suiteble envirment with needed tools like maven
# Second step takes the artifact (*.jar) and put it in a minimalitic envirment just enough to run to make the 
# image as small as possible

FROM maven:3.9-amazoncorretto-17 as builder
WORKDIR /app
COPY pom.xml pom.xml 
COPY src/ src/
RUN mvn package

FROM amazoncorretto:17.0.9-alpine3.18
COPY --from=builder /app/target/*.jar /app/application.jar
CMD ["java","-jar","/app/application.jar"]