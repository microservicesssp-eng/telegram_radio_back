# Stage 1: извлекаем слои из fat-jar
FROM eclipse-temurin:17-jre AS builder
WORKDIR /builder
ARG JAR_FILE=build/libs/telegram_radio-*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# Stage 2: runtime-образ, по одному слою на COPY
FROM eclipse-temurin:17-jre
WORKDIR /application
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-XX:+UseSerialGC", "-Xss256k", "-jar", "application.jar"]
