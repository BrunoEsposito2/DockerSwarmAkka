# Use a standard OpenJDK image instead of docker:dind
FROM openjdk:17-jdk-alpine

# Install bash and other necessary tools
RUN apk add --no-cache bash wget curl

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-7.6-bin.zip -O /tmp/gradle.zip \
    && unzip /tmp/gradle.zip -d /opt \
    && rm /tmp/gradle.zip \
    && mv /opt/gradle-7.6 /opt/gradle \
    && ln -s /opt/gradle/bin/gradle /usr/bin/gradle

# Set working directory
WORKDIR /app

# Copy the entire project (since context is root)
COPY . /app/

# Make entrypoint executable
RUN chmod +x /app/node1/entrypoint.sh

# Expose ports
EXPOSE 2555 4000

# Use the simplified entrypoint
CMD ["/bin/bash", "/app/node1/simple-entrypoint.sh"]