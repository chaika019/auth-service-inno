FROM maven:3.9-amazoncorretto-25-al2023 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/authservice-0.0.1.jar auth-service.jar
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "auth-service.jar"]