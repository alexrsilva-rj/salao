# ============================================================
# Multi-stage build — Hardening de container (Issue 13 / CWE-250)
# ============================================================

# Stage 1: Build da aplicação
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN chmod +x ./gradlew && ./gradlew :salao-api:bootJar --no-daemon -x test

# Stage 2: Runtime enxuta com usuário não-root (UID/GID 10001)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar grupo e usuário não-privilegiados com ID numérico 10001
RUN addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup

COPY --from=builder /build/salao-api/build/libs/salao-api-*.jar app.jar

RUN chown -R appuser:appgroup /app

# Executar como usuário não-root (UID 10001)
USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
