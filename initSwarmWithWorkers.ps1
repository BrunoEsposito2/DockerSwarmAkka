# Inizializza lo Swarm
docker swarm init

# Ottieni il token di join per i worker
$token = docker swarm join-token worker -q

# Ottieni l'indirizzo IP del manager
$managerIP = docker info --format '{{.Swarm.NodeAddr}}'

# Crea una rete overlay per i worker
docker network create --driver overlay worker_network

# Crea i servizi worker come parte dello Swarm
docker service create --name worker1 --network worker_network `
    --mode global `
    alpine sleep infinity

docker service create --name worker2 --network worker_network `
    --mode global `
    alpine sleep infinity

# Attendi che i servizi siano attivi
Start-Sleep -Seconds 30

# Verifica lo stato dei servizi
docker service ls

# Verifica i nodi nello Swarm
docker node ls

Write-Host "Setup completato. Verifica i nodi sopra."