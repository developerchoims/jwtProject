# Stage 1: Build
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /app

# Copy source code and build
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Make gradlew executable
RUN chmod +x ./gradlew

# Run Gradle build
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Run
FROM gradle:8.8-jdk17-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]