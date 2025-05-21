#!/bin/bash

# AKKA Production Deployment Script
# Questo script automatizza il deployment di AKKA usando Docker Swarm

# Colori per l'output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurazione 
VERSION=$1
ORGANIZATION="brunoesposito2"

if [ -z "$VERSION" ]; then
    echo -e "${YELLOW}Nessuna versione specificata, utilizzo 'latest'${NC}"
    VERSION="latest"
fi

echo -e "${BLUE}=== AKKA Deployment Script - Versione $VERSION ===${NC}"

# Verifica se docker è installato
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker non è installato. Per favore installa Docker prima di continuare.${NC}"
    exit 1
fi

# Verifica se lo swarm è già inizializzato
swarm_status=$(docker info --format '{{.Swarm.LocalNodeState}}' 2>/dev/null)
if [ "$swarm_status" != "active" ]; then
    echo -e "${YELLOW}Inizializzazione Docker Swarm...${NC}"
    docker swarm init || { 
        echo -e "${RED}Errore durante l'inizializzazione dello swarm${NC}"
        echo -e "${YELLOW}Provo con l'opzione --advertise-addr${NC}"
        
        # Ottieni l'indirizzo IP predefinito
        DEFAULT_IP=$(hostname -I | awk '{print $1}')
        docker swarm init --advertise-addr $DEFAULT_IP || {
            echo -e "${RED}Inizializzazione swarm fallita. Verifica la tua configurazione di rete.${NC}"
            exit 1
        }
    }
else
    echo -e "${GREEN}Docker Swarm è già attivo.${NC}"
fi

# Ottieni il token di join per i worker
JOIN_TOKEN=$(docker swarm join-token -q worker)
echo -e "${GREEN}Token di join dello swarm: $JOIN_TOKEN${NC}"

# Ottieni l'IP del manager
MANAGER_IP=$(docker node inspect self --format '{{.Status.Addr}}')
echo -e "${GREEN}IP del manager: $MANAGER_IP${NC}"

# Crea la rete overlay se non esiste
if ! docker network ls | grep -q "swarm-network"; then
    echo -e "${YELLOW}Creazione rete overlay 'swarm-network'...${NC}"
    docker network create --driver overlay --attachable swarm-network || { 
        echo -e "${RED}Errore durante la creazione della rete${NC}"
        exit 1
    }
else
    echo -e "${GREEN}Rete 'swarm-network' già presente.${NC}"
fi

# Scarica il docker-compose file
echo -e "${YELLOW}Scaricamento del file docker-compose...${NC}"
COMPOSE_URL="https://github.com/brunoesposito2/DockerSwarmAkka/releases/download/v${VERSION}/docker-compose-${VERSION}.yml"
COMPOSE_FILE="docker-compose-${VERSION}.yml"

if curl -sSL "$COMPOSE_URL" -o "$COMPOSE_FILE"; then
    echo -e "${GREEN}File docker-compose scaricato con successo.${NC}"
else
    echo -e "${RED}Impossibile scaricare il file docker-compose. Verificando localmente...${NC}"
    if [ ! -f "$COMPOSE_FILE" ]; then
        echo -e "${RED}File '$COMPOSE_FILE' non trovato. Scarica il file manualmente dalla pagina delle release GitHub.${NC}"
        exit 1
    else
        echo -e "${GREEN}Utilizzando il file docker-compose locale.${NC}"
    fi
fi

# Export delle variabili d'ambiente necessarie
export JOIN_TOKEN=$JOIN_TOKEN
export MANAGER_IP=$MANAGER_IP

# Verifica se ci sono stack esistenti e li rimuove
if docker stack ls | grep -q "akka"; then
    echo -e "${YELLOW}Trovato stack AKKA esistente. Rimozione...${NC}"
    docker stack rm akka
    echo -e "${YELLOW}Attesa per la rimozione completa dello stack...${NC}"
    sleep 15  # Attendi che lo stack venga rimosso completamente
fi

# Deploy dello stack
echo -e "${YELLOW}Deploying AKKA stack...${NC}"
docker stack deploy -c "$COMPOSE_FILE" akka || {
    echo -e "${RED}Errore durante il deployment dello stack.${NC}"
    exit 1
}

# Verifica che tutti i servizi siano avviati
echo -e "${YELLOW}Verifica dello stato dei servizi...${NC}"
sleep 5  # Attendi che i servizi inizino
docker stack services akka

echo -e "${GREEN}=== Deployment completato! ===${NC}"
echo -e "${BLUE}Per visualizzare i log, esegui: docker service logs -f <service_name>${NC}"
echo -e "${BLUE}Per monitorare i servizi, esegui: docker stack services akka${NC}"