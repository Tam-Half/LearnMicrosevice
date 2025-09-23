# Run stage
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/OrderService-0.0.1-SNAPSHOT.jar /run/OrderService-0.0.1-SNAPSHOT.jar

EXPOSE 8083

ENTRYPOINT java -jar /run/OrderService-0.0.1-SNAPSHOT.jar.jar --spring.config.location=/run/src/main/resources/application.properties

