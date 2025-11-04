FROM openjdk:17.0.1
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-Djdk.internal.platform.disableCgroup=true","-Dspring.main.lazy-initialization=true","-jar","app.jar"]
EXPOSE 8081
