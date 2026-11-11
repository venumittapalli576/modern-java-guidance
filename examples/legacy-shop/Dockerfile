FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/legacy-shop-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS="-Xmx512m -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC -XX:+PrintGCDateStamps"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]
