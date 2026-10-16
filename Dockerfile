# Multi-stage Dockerfile for Spring Boot Application on Railway
# Stage 1: Build JAR with Maven and OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/campusgrid-core-1.0.0.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
