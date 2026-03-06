FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y fontconfig libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

COPY target/*.jar app.jar

EXPOSE 8080

RUN groupadd -r climacode && useradd -r -g climacode climacode
RUN chown -R climacode:climacode /app
USER climacode

ENTRYPOINT ["java", "-jar", "app.jar"]