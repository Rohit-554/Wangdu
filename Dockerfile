# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy gradle wrapper and build scripts first for better layer caching
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./

# Copy modules needed to build the server
COPY shared ./shared
COPY server ./server

RUN chmod +x ./gradlew

# Build a self-contained runnable distribution of the server
RUN ./gradlew :server:installDist --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Persisted SQLite location (mount a Render disk here to keep data)
ENV DB_PATH=/data/whiteboard.db
RUN mkdir -p /data

COPY --from=build /app/server/build/install/server ./

# Render injects PORT at runtime; the app reads it (defaults to 8080)
EXPOSE 8080

CMD ["./bin/server"]
