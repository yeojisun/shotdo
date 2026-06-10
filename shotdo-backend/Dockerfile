# Stage 1: Build the application
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle* .

# Grant execute permission and fetch dependencies
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copy source code and build jar
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Create final runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/app/build/libs/shotdo-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Spring Boot default is 8080, but Render uses PORT environment variable automatically)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
