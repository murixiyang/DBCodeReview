# Stage 1: build with Maven
FROM maven:3.8.7-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: run the fat JAR
FROM eclipse-temurin:17-jdk
WORKDIR /app

RUN mkdir -p /app/data

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
# Tell Spring to bind to 0.0.0.0:8081
ENTRYPOINT ["java","-Dserver.address=0.0.0.0","-Dserver.port=8081","-jar","app.jar"]
