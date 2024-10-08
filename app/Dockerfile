FROM ubuntu:latest

# Installa wget e unzip
RUN apt-get update && \
    apt-get install -y wget unzip && \
    apt-get clean

# Installa JRE 17
RUN apt-get update && \
    apt-get install -y openjdk-17-jre && \
    apt-get clean

# Installa Gradle
RUN wget https://services.gradle.org/distributions/gradle-7.6-bin.zip && \
    unzip gradle-7.6-bin.zip && \
    mv gradle-7.6 /usr/local/gradle && \
    rm gradle-7.6-bin.zip && \
    echo "export PATH=$PATH:/usr/local/gradle/bin" >> /etc/profile.d/gradle.sh

WORKDIR /nodeone

COPY . /nodeone

CMD ["/usr/local/gradle/bin/gradle", "runScalaMain"]