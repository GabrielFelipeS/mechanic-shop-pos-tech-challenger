FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder --chown=spring:spring /app/target/mechanic-shop-0.0.1-SNAPSHOT.jar app.jar
USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"]