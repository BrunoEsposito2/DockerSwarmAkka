---
layout: default
title: Docker Compose API v0
---

# Docker Compose Public API v0

**Version:** 0.0.10  
**Generated:** 2025-05-29 14:16:29 UTC

## API Overview

This document describes the public interface for deploying the Akka cluster using Docker Compose.

## Public Services

### worker1

```yaml
build:
  context: ./node1/
  dockerfile: Dockerfile
privileged: true
container_name: worker1
# Public API: Akka remoting port for primary node
# Port 2555 is the canonical port for worker1 cluster communication
# Changing this port breaks cluster formation - requires MAJOR version bump
ports:
  - "2555:2555"
# Public API: Required environment variables for cluster deployment
# These variables must be provided for successful cluster formation
environment:
  # Docker Swarm join token - required for swarm node joining
  - JOIN_TOKEN=${JOIN_TOKEN}
  # Docker Swarm manager IP address - required for swarm communication  
  - MANAGER_IP=${MANAGER_IP}
volumes:
  - worker1-certs:/certs
# Public API: Cluster overlay network
# Network name 'swarm-network' is part of deployment interface
# Changing network name breaks inter-service communication
networks:
  - swarm-network
```

### worker2

```yaml
build:
  context: ./node2/
  dockerfile: Dockerfile
privileged: true
container_name: worker2
# Public API: Akka remoting port for secondary node
# Port 2551 is the canonical port for worker2 cluster communication
# Changing this port breaks cluster formation - requires MAJOR version bump
ports:
  - "2551:2551"
# Public API: Required environment variables (same as worker1)
environment:
  - JOIN_TOKEN=${JOIN_TOKEN}
  - MANAGER_IP=${MANAGER_IP}
volumes:
  - worker2-certs:/certs
networks:
  - swarm-network
```

## Networks

```yaml
swarm-network:
  driver: overlay
  attachable: true
```
