FROM eclipse-temurin:27-jdk AS build

WORKDIR /app

COPY .. .

RUN chmod +x gradlew
RUN ./gradlew :web:bootJar --no-daemon


FROM eclipse-temurin:27-jre

WORKDIR /app

COPY --from=build /app/web/build/libs/*.jar application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]