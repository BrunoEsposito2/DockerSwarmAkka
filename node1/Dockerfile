FROM docker:dind

# Installare gli strumenti necessari (inclusi quelli per apt-get)
RUN apk add --no-cache bash \
       && apk add --no-cache --virtual .build-deps wget \
       && apk add --no-cache --update openjdk17 \
       && wget https://services.gradle.org/distributions/gradle-7.6-bin.zip -O /tmp/gradle.zip \
       && unzip /tmp/gradle.zip -d /opt \
       && rm /tmp/gradle.zip \
       && mv /opt/gradle-7.6 /opt/gradle \
       && ln -s /opt/gradle/bin/gradle /usr/bin/gradle \
       && apk del .build-deps

# Impostare la directory di lavoro
WORKDIR /node1

# Copiare i file
COPY . /node1

CMD ["/bin/sh", "/node1/entrypoint.sh"]