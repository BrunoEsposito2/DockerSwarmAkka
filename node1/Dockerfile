# Use OpenJDK as base
FROM openjdk:17-jdk-alpine

USER root

# Install Docker and other necessary tools
RUN apk add --no-cache \
    bash \
    wget \
    curl \
    docker \
    docker-compose \
    shadow

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-7.6-bin.zip -O /tmp/gradle.zip \
    && unzip /tmp/gradle.zip -d /opt \
    && rm /tmp/gradle.zip \
    && mv /opt/gradle-7.6 /opt/gradle \
    && ln -s /opt/gradle/bin/gradle /usr/bin/gradle

# Add user to docker group (if needed)
RUN addgroup -S docker || true

# Set working directory
WORKDIR /app

# Copy the entire project
COPY . /app/

# Make entrypoint executable
RUN chmod +x /app/entrypoint.sh

# Expose ports
EXPOSE 2555 4000

# Run entrypoint with privileged mode required for Docker
CMD ["/bin/bash", "/app/entrypoint.sh"]