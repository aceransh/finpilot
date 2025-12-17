# Stage 1: Build the application using Maven and Java 23
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (for caching)
COPY pom.xml .
# Copy the actual source code
COPY src ./src

# Build the jar file, skipping tests to save time
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image to run the app
FROM eclipse-temurin:23-jre
WORKDIR /app

# Copy the built jar from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (Standard for Spring Boot)
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]