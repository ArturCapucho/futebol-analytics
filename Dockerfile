# --- build: compila e empacota com o Maven Wrapper ---------------------------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Dependências primeiro: esta camada só é refeita quando o pom muda.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# --- runtime: só o JRE e o jar, rodando sem root -----------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 app
COPY --from=build /app/target/futebol-analytics-*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
