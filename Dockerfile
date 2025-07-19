# Start with a base image containing Java runtime
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy the built jar from target/ (you must build locally first)
COPY target/app-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on (same as in application.yml)
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]