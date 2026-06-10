FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the fat JAR built by Maven
COPY target/api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]
