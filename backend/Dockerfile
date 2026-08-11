# Render has no native Java runtime — this builds and runs the Spring Boot backend via Docker.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/booknook-backend.jar app.jar
# Render injects PORT at runtime; application.yml already reads it via ${PORT:8080}.
ENTRYPOINT ["java", "-jar", "app.jar"]
