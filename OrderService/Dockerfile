# Run stage
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar /run/*.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "*.jar"] --spring.config.location=/run/src/main/resources/application.properties

