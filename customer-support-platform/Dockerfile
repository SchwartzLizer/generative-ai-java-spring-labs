FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress -pl customer-support-platform -am package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --create-home --uid 10001 appuser
WORKDIR /app
COPY --from=build /workspace/customer-support-platform/target/customer-support-platform-1.0.0-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
