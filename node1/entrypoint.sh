#!/bin/bash

# Function to check whether the Docker daemon is running
check_docker() {
    docker info >/dev/null 2>&1
    return $?
}

# Start the Docker daemon in the background
echo "Starting Docker deamon..."
dockerd &

# Wait for the Docker daemon to be ready
echo "Waiting for the initialisation of the Docker daemon..."
WAIT_SECONDS=0
while ! check_docker; do
    sleep 2
    WAIT_SECONDS=$((WAIT_SECONDS + 2))
    echo "Waiting for the initialisation of the Docker daemon... ($WAIT_SECONDS seconds)"

    if [ $WAIT_SECONDS -ge 60 ]; then
        echo "Timeout: The Docker daemon has not started within 60 seconds."
        exit 1
    fi
done

echo "The Docker daemon is ready!"

# Run Docker commands
docker swarm join --token "$JOIN_TOKEN" "$MANAGER_IP:2377"
sleep 5
gradle runScalaMain