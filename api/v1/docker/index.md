---
layout: default
title: Docker Compose API v1
---

# Docker Compose Public API v1

**Version:** 1.0.6  
**Generated:** 2025-05-31 15:52:56 UTC

## API Overview

This document describes the public interface for deploying the Akka cluster using Docker Compose.

## Public Services

| Service | Ports | Networks |
|---------|-------|----------|
| **worker1** | `2555:2555` | `swarm-network` |
| **worker2** | `2551:2551` | `swarm-network` |

## Service Details

### worker1

**Configuration:**
- **Ports:** `2555:2555`
- **Networks:** `swarm-network`
- **Container Name:** `worker1`

### worker2

**Configuration:**
- **Ports:** `2551:2551`
- **Networks:** `swarm-network`
- **Container Name:** `worker2`

## Networks

### swarm-network

- **Driver:** `overlay`
- **Attachable:** `true`

## Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `JOIN_TOKEN` | Docker Swarm worker join token | `SWMTKN-1-...` |
| `MANAGER_IP` | Docker Swarm manager IP address | `192.168.1.100` |

## Usage Example

```bash
# Set required environment variables
export JOIN_TOKEN=$(docker swarm join-token -q worker)
export MANAGER_IP=$(docker node inspect self --format '{{.Status.Addr}}')

# Deploy the cluster
docker-compose up -d
```
