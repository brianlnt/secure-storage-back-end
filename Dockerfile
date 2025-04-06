# Use JDK 17 as base image
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/secure-storage-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"] 