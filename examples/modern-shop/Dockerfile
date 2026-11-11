FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/modern-shop-0.0.1-SNAPSHOT.jar app.jar
USER 1000
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
