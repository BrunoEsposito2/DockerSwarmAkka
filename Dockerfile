# Usa Alpine come immagine di base
FROM alpine:latest

# Installa bash e Docker
RUN apk add --no-cache bash docker

# Installa alcuni strumenti utili
RUN apk add --no-cache curl iputils

# Imposta bash come shell predefinita
SHELL ["/bin/bash", "-c"]

# Comando da eseguire all'avvio del container
CMD ["/bin/bash"]