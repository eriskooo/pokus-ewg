# Jednoduchý Dockerfile založený na OpenJDK 21 (Temurin JRE)
# Očakáva, že Maven build vyprodukuje uber-jar (nastavené v pom.xml)

FROM eclipse-temurin:21-jre as runtime

WORKDIR /app

# Voliteľné: nastavenie neprivilegovaného používateľa
RUN useradd -r -u 1001 appuser && \
    mkdir -p /app && chown -R 1001:0 /app
USER 1001

# Skopírujeme jar z target/ (názov bude podľa artifactId a verzie)
# Pri CI/CD môžete použiť presný názov alebo konvenciu s ARG
ARG JAR_FILE=target/pokus-ewg-1.0.0-SNAPSHOT-runner.jar
COPY ${JAR_FILE} /app/app.jar

# Quarkus načíta externú konfiguráciu ak nastavíme QUARKUS_CONFIG_LOCATIONS
ENV QUARKUS_CONFIG_LOCATIONS=/etc/pokus-ewg-config

# Exponujeme default port Quarkusu
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
