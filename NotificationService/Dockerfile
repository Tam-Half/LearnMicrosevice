FROM eclipse-temurin:17-jdk
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-Dmanagement.metrics.enable.processor=false", "-jar", "/app.jar"]
EXPOSE 8089