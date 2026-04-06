# Multi-stage build — final image target < ~300MB (JRE + single fat JAR).
# Build from repo root: docker build -t bank-api:local .

FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
RUN apk add --no-cache bash
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./
COPY bank-shared/pom.xml bank-shared/
COPY bank-iam/pom.xml bank-iam/
COPY bank-accounts/pom.xml bank-accounts/
COPY bank-audit/pom.xml bank-audit/
COPY bank-payments/pom.xml bank-payments/
COPY bank-loans/pom.xml bank-loans/
COPY bank-notifications/pom.xml bank-notifications/
COPY bank-boot/pom.xml bank-boot/
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline -DskipTests || true
COPY . .
RUN ./mvnw -q -B -pl bank-boot -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl \
	&& addgroup -S bank && adduser -S bank -G bank
COPY --from=builder /build/bank-boot/target/bank-boot-*.jar app.jar
USER bank
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseZGC"
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
	CMD curl -fsS http://127.0.0.1:8080/actuator/health >/dev/null || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
