# syntax=docker/dockerfile:1.7

# ---------------------------------------------------------------------------
# Etapa 1: compilacion (imagen pesada, NO viaja a produccion)
# ---------------------------------------------------------------------------
FROM docker.io/library/maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Capa de dependencias: solo se invalida si cambia el pom.xml
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline

# Capa de codigo: cambia en cada commit
COPY src ./src
# Los tests corren en CI; aqui solo se empaqueta
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests -Djacoco.skip=true

# Divide el JAR en capas (dependencias / loader / snapshots / aplicacion)
RUN java -Djarmode=tools -jar target/app.jar extract --layers --destination /workspace/extracted

# JRE minimo a la medida: jdeps detecta los modulos que usa la aplicacion y jlink
# construye un runtime solo con ellos (el JRE completo pesa ~210 MB, este ~80 MB).
# Los modulos extra cubren lo que jdeps no ve (reflexion, TLS, JDBC, etc.).
RUN jdeps --ignore-missing-deps -q --recursive --multi-release 21 --print-module-deps \
        --class-path "/workspace/extracted/dependencies/lib/*" \
        /workspace/extracted/application/app.jar > /workspace/modules.txt \
 && jlink --add-modules "$(cat /workspace/modules.txt),java.sql,java.naming,java.desktop,java.management,jdk.management,java.instrument,java.security.jgss,java.security.sasl,java.xml,jdk.crypto.ec,jdk.unsupported,jdk.zipfs" \
        --strip-debug --no-man-pages --no-header-files --compress=zip-6 \
        --output /javaruntime \
 && /javaruntime/bin/java -Xshare:dump

# ---------------------------------------------------------------------------
# Etapa 2: runtime (Alpine minimo + JRE a la medida + capas de la aplicacion)
# ---------------------------------------------------------------------------
FROM docker.io/library/alpine:3.21 AS runtime

RUN apk add --no-cache libstdc++ zlib \
 && addgroup -S app && adduser -S -G app app
COPY --from=build /javaruntime /opt/java
ENV JAVA_HOME=/opt/java \
    PATH="/opt/java/bin:${PATH}"
WORKDIR /app
USER app

# De la capa que menos cambia a la que mas cambia (mejor cache y pushes pequenos)
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -Xshare:auto -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- "http://localhost:${PORT}/actuator/health/readiness" || exit 1

# exec: la JVM es PID 1 y recibe SIGTERM (apagado ordenado)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
