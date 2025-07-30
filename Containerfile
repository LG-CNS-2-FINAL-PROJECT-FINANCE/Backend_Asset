FROM gradle:8.14.2-jdk17 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle clean bootJar

# 2단계: 런타임 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]