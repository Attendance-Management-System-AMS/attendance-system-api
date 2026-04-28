# Stage 1: Build
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY hr-service/pom.xml hr-service/pom.xml
COPY attendance-service/pom.xml attendance-service/pom.xml
COPY monolith-service/pom.xml monolith-service/pom.xml
COPY monolith-service/src ./monolith-service/src
# Build monolith trong lúc các microservice đang được tách dần.
RUN mvn -pl monolith-service -am clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# Copy file jar từ module monolith-service.
COPY --from=build /app/monolith-service/target/monolith-service-0.0.1-SNAPSHOT.jar app.jar

# Cổng mặc định của Spring Boot
EXPOSE 9000

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
