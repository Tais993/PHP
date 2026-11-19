# Build backend
FROM eclipse-temurin:27-jdk AS backend-build

WORKDIR /backend

COPY backend/ ./

RUN chmod +x gradlew
RUN ./gradlew :web:bootJar --no-daemon


# Run application
FROM eclipse-temurin:27-jre

WORKDIR /app

COPY --from=backend-build /backend/web/build/libs/*.jar application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
