# Build frontend
FROM node:24-alpine AS frontend-build

WORKDIR /frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build


# Build backend, including the frontend output
FROM eclipse-temurin:27-jdk AS backend-build

WORKDIR /backend

COPY backend/ ./

COPY --from=frontend-build /frontend/dist/ ./web/src/main/resources/static/

RUN chmod +x gradlew
RUN ./gradlew :web:bootJar --no-daemon


# Run application
FROM eclipse-temurin:27-jre

WORKDIR /app

COPY --from=backend-build /backend/web/build/libs/*.jar application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]