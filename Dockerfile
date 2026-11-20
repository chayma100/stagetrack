# Image unique : build Angular, puis build Spring Boot (avec Angular dans static/), puis exécution.
# Utilisé par Render (Runtime = Docker).

# ---------- 1) Build Angular ----------
FROM node:18-alpine AS front
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install --legacy-peer-deps --no-audit --no-fund
COPY frontend/ ./
RUN npx ng build --configuration production

# ---------- 2) Build Spring Boot avec Angular dans static/ ----------
FROM maven:3.9-eclipse-temurin-17 AS back
WORKDIR /build
COPY backend/pom.xml .
RUN mvn -q dependency:go-offline
COPY backend/src ./src
COPY --from=front /app/dist/ ./src/main/resources/static/
RUN mvn -q -DskipTests clean package

# ---------- 3) Exécution ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=back /build/target/psm.jar /app/psm.jar
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
# Render fournit le port dans la variable PORT (10000 par défaut), lue par Spring Boot
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/psm.jar"]
