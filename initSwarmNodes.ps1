# Numero di nodi da creare
$NUM_NODES = 3

# Crea i nodi
1..$NUM_NODES | ForEach-Object {
    docker run -d --privileged --name node$_ --hostname=node$_ docker:dind
}

# Attendo che i demoni Docker siano pronti
Start-Sleep -Seconds 10

docker swarm init

# Ottengo il token di join per i worker
$SWARM_TOKEN = docker swarm join-token worker -q

# Ottengo l'indirizzo IP del nodo manager
$MANAGER_IP = docker info --format '{{.Swarm.NodeAddr}}'

# Aggiungo i nodi allo Swarm
1..$NUM_NODES | ForEach-Object {
    docker exec node$_ docker swarm join --token $SWARM_TOKEN ${MANAGER_IP}:2377
}

# Verifico lo stato dello Swarm
docker node ls