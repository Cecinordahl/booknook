# Render has no native Java runtime — this builds and runs the Spring Boot backend via Docker.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -B clean package -DskipTests

# Not -alpine: grpc-netty-shaded's native tcnative library (used by the Firestore client) is
# built against glibc and segfaults under Alpine's musl libc. jammy (Ubuntu-based) has glibc.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/booknook-backend.jar app.jar
# Render injects PORT at runtime; application.yml already reads it via ${PORT:8080}.
ENTRYPOINT ["java", "-jar", "app.jar"]
