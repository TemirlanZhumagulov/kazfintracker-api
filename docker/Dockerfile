# Use a base image with the desired JRE (e.g., OpenJDK 11)
FROM adoptopenjdk:11-jre-hotspot

WORKDIR /app

COPY /build/libs/sandbox-server.jar ./app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
