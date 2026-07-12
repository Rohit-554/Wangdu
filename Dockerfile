# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# The :shared module applies the Android plugin, so Gradle needs ANDROID_HOME
# to point at an existing directory just to configure the project. No real SDK
# is needed — building :server only compiles shared's JVM code, so an empty
# directory is enough.
ENV ANDROID_HOME=/opt/android-sdk
RUN mkdir -p /opt/android-sdk

# Copy gradle wrapper and build scripts first for better layer caching
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts gradle.properties ./
# Trimmed settings so only :server and :shared are configured (the app modules
# aren't copied into the container).
COPY deploy/settings.gradle.kts ./settings.gradle.kts

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
