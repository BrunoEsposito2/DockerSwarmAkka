FROM openjdk:17-alpine

# Installare gli strumenti necessari
RUN apk add --no-cache bash \
    && apk add --no-cache --virtual .build-deps wget unzip \
    && wget https://services.gradle.org/distributions/gradle-7.6-bin.zip -O /tmp/gradle.zip \
    && unzip /tmp/gradle.zip -d /opt \
    && rm /tmp/gradle.zip \
    && mv /opt/gradle-7.6 /opt/gradle \
    && ln -s /opt/gradle/bin/gradle /usr/bin/gradle \
    && apk del .build-deps

# Installare Docker (client e daemon)
RUN apk add --no-cache docker openrc \
    && mkdir -p /run/openrc \
    && touch /run/openrc/softlevel \
    && rc-update add docker default

# Impostare la directory di lavoro
WORKDIR /project

# Copiare i file
COPY . /project

CMD ["/bin/sh", "./node1/entrypoint.sh"]