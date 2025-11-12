FROM  openjdk:17.0.1
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-Dmanagement.metrics.enable.processor=false", "-jar", "/app.jar"]
EXPOSE 8085