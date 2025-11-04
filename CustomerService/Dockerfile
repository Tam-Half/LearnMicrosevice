FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-Djdk.internal.platform.disableCgroup=true","-Dspring.main.lazy-initialization=true","-jar","app.jar"]
EXPOSE 8081
