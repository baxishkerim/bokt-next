# --- Стадия сборки ---
FROM gradle:8.10-jdk21 AS build
WORKDIR /workspace
COPY . .
# офлайн-кэш недоступен при первой сборке — Gradle скачает зависимости
RUN gradle :api:bootJar --no-daemon

# --- Стадия запуска ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api/build/libs/bokt-next.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
