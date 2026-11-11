# Stage 1: Build fat JAR
FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle shadowJar --no-daemon --info

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S choreemon && adduser -S choreemon -G choreemon
COPY --from=builder /app/build/libs/choreemon-backend-1.0.0.jar app.jar
RUN mkdir -p /app/uploads && chown choreemon:choreemon /app/uploads
USER choreemon
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
