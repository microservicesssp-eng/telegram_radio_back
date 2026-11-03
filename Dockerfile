# Stage 1: сборка JAR внутри контейнера
FROM gradle:8.14.2-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# Stage 2: runtime
FROM eclipse-temurin:17-jre
WORKDIR /application
COPY --from=builder /app/build/libs/telegram_radio-*.jar application.jar
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-XX:+UseSerialGC", "-Xss256k", "-jar", "application.jar"]
