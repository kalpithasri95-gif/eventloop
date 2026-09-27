# ==============================================================================
# EVENTLOOP - 24/7 Cloud Dockerfile (Zero-Host-Involvement Deployment)
# Compatible with Render, Railway, Fly.io, AWS, Heroku & any Cloud Container
# ==============================================================================

# Build Stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy dependency JARs and Source Code
COPY lib ./lib
COPY src ./src

# Compile Java source files
RUN mkdir -p bin && \
    javac -encoding UTF-8 -cp "lib/*" -d bin $(find src -name "*.java")

# Production Runtime Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy compiled classes, libraries, and web assets
COPY --from=builder /app/bin ./bin
COPY lib ./lib
COPY web ./web

# Cloud providers dynamically pass PORT environment variable
ENV PORT=8080
EXPOSE 8080

# Launch EventLoop Web Server (Classpath separated by ':' on Linux)
CMD ["sh", "-c", "java -cp \"bin:lib/*\" com.eventloop.web.EventLoopWebServer"]
