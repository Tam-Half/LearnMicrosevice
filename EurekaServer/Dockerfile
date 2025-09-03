# Use a modern, supported base image
FROM openjdk:17-jdk-slim

# Copy the compiled .jar file of your Eureka service
# (Assuming your Spring Boot project creates a jar in the 'target' directory)
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8761

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]