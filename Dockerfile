# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copy both modules
COPY messenger/ messenger/
COPY API/       API/

# Install messenger into the local Maven cache, then package the API
RUN mvn -f messenger/pom.xml install -DskipTests \
 && mvn -f API/pom.xml package -DskipTests

# ─── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /build/API/target/api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]
