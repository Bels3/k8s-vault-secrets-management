# -------------------------------------------------------
# Stage 1: Build
# -------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# -------------------------------------------------------
# Stage 2: Runtime
# -------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Non-root user - production security best practice
RUN groupadd --system insurance && \
    useradd --system --gid insurance --no-create-home insurance

COPY --from=builder /build/target/policy-service-1.0.0.jar app.jar

RUN chown insurance:insurance app.jar

USER insurance

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
