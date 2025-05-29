---
layout: default
title: API Documentation v0
---

# Public API Documentation v0

**Current Version:** 0.0.10  
**Generated:** 2025-05-29 15:08:23 UTC

## 📨 Message Protocol API

- [**Scala API Documentation**](./scala/) - Complete documentation for both nodes
  - [Shared Public API](./scala/shared/) - Common interfaces and protocols
  - [Node1 Implementation](./scala/node1/) - Primary cluster node (Ping service)
  - [Node2 Implementation](./scala/node2/) - Secondary cluster node (Pong service)

## 🐳 Deployment API

- [**Docker Compose Interface**](./docker/) - Service configuration and networking

## 🏗️ Architecture Overview

This project implements a distributed Akka cluster with two specialized nodes:

### Node Architecture

| Node | Role | Port | Service | Responsibility |
|------|------|------|---------|----------------|
| **Node1** | Primary | 2555 | Ping | Cluster coordination, ping initiation |
| **Node2** | Secondary | 2551 | Pong | Message response, worker services |

### Communication Flow

```
Node1 (Ping Service)  ←→  Node2 (Pong Service)
    ↓                           ↓
ServiceKeys.PingServiceKey  ServiceKeys.PongServiceKey
    ↓                           ↓
Receptionist Registration  Receptionist Registration
```

## 🔄 Version History

- [All API Versions](../index.html) - Browse historical API documentation
- [Migration Guide](https://github.com/brunoesposito2/DockerSwarmAkka/releases) - Breaking changes between versions

## 📋 Semantic Versioning

This API follows [Semantic Versioning 2.0.0](https://semver.org/):

| Version Component | When to Increment | Examples |
|------------------|-------------------|----------|
| **MAJOR** (breaking) | Incompatible changes | Message protocol changes, port changes, service renames |
| **MINOR** (feature) | Backward compatible features | New message types, optional configurations |
| **PATCH** (fix) | Backward compatible fixes | Bug fixes, documentation updates |

## 🚀 Quick Start

### 1. Using the Message Protocol

```scala
import org.example.api.protocol.{Ping, Pong}
import org.example.api.discovery.ServiceKeys

// Register services (both nodes)
context.system.receptionist ! Receptionist.Register(ServiceKeys.PingServiceKey, context.self)
context.system.receptionist ! Receptionist.Register(ServiceKeys.PongServiceKey, context.self)

// Send messages
pingService ! Ping(context.self)
pongService ! Pong(context.self)
```

### 2. Deploying with Docker Compose

```bash
# Set required environment variables
export JOIN_TOKEN=$(docker swarm join-token -q worker)
export MANAGER_IP=$(docker node inspect self --format '{{.Status.Addr}}')

# Deploy both nodes
docker-compose up -d
```

### 3. Monitoring the Cluster

```bash
# Check cluster status
docker node ls

# View service logs
docker service logs akka_worker1
docker service logs akka_worker2

# Monitor ping-pong communication
docker logs -f worker1
docker logs -f worker2
```

## 📖 Documentation Guide

### For API Users
1. Start with [Shared Public API](./scala/shared/) for stable interfaces
2. Review [Docker API](./docker/) for deployment configuration
3. Check version compatibility in [Migration Guide](https://github.com/brunoesposito2/DockerSwarmAkka/releases)

### For Implementers
1. Study [Node1](./scala/node1/) for primary node implementation patterns
2. Review [Node2](./scala/node2/) for secondary node architecture
3. Understand the complete flow in [Shared API](./scala/shared/)

### For DevOps
1. Configure deployment using [Docker API](./docker/) documentation
2. Set up monitoring based on service definitions
3. Plan capacity using the architecture overview above

---

*This documentation is automatically generated and represents the complete API surface for version 0.0.10*
