#!/bin/bash

# Function to check whether the Docker daemon is running
check_docker() {
    docker info >/dev/null 2>&1
    return $?
}

# Start the Docker daemon in the background
if ! check_docker; then
    echo "Starting Docker daemon with DinD configuration..."
    
    # Start dockerd with specific flags for containers
    dockerd \
        --host=unix:///var/run/docker.sock \
        --storage-driver=vfs \
        --iptables=false \
        --ip-forward=false \
        --bridge=none \
        --exec-opt native.cgroupdriver=cgroupfs \
        --log-level=error \
        >/dev/null 2>&1 &

    echo "Waiting for Docker daemon initialization..."
    WAIT_SECONDS=0
    while ! check_docker; do
        sleep 2
        WAIT_SECONDS=$((WAIT_SECONDS + 2))
        echo "Waiting for Docker daemon... ($WAIT_SECONDS seconds)"

        if [ $WAIT_SECONDS -ge 60 ]; then
            echo "Timeout: Docker daemon has not started within 60 seconds."
            echo "Trying alternative approach..."
            break
        fi
    done
else
    echo "Docker daemon is already running!"
fi

echo "The Docker daemon is ready!"

# Run Docker commands
docker swarm join --token "$JOIN_TOKEN" "$MANAGER_IP:2377"
sleep 5
gradle runScalaMain