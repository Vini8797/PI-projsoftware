# ---------- Estágio 1: compila (imagem pesada, com Maven + JDK) ----------
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copia só o pom primeiro: se o pom não mudou, o Docker reaproveita o cache das dependências
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

# -DskipTests: os testes rodam no GitHub Actions. Aqui o Testcontainers precisaria de Docker dentro do Docker.
RUN mvn clean package -DskipTests

# ---------- Estágio 2: roda (imagem leve, só JRE + o jar) ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

# *.jar funciona com qualquer nome de artifact
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
