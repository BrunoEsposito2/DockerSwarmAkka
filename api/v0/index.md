---
layout: default
title: API Documentation v0
---

# Public API Documentation v0

**Current Version:** 0.0.10  
**Generated:** 2025-05-29 15:27:44 UTC

## 📨 Message Protocol API

- [**Scala API Documentation**](./scala/) - Complete documentation for both nodes
  - [Node1 (Primary)](./scala/node1/) - Ping service implementation
  - [Node2 (Secondary)](./scala/node2/) - Pong service implementation

## 🐳 Deployment API

- [**Docker Compose Interface**](./docker/) - Service configuration and networking

## 🏗️ Architecture Overview

| Node | Role | Port | Service | Documentation |
|------|------|------|---------|---------------|
| **Node1** | Primary | 2555 | Ping | [View Docs](./scala/node1/) |
| **Node2** | Secondary | 2551 | Pong | [View Docs](./scala/node2/) |

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

// Register services
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
docker logs -f worker1
docker logs -f worker2
```

## 📖 Documentation Guide

- **For API Users**: Check both [Node1](./scala/node1/) and [Node2](./scala/node2/) for complete interface coverage
- **For Deployment**: Use [Docker API](./docker/) for service configuration
- **For Migration**: Review [Release Notes](https://github.com/brunoesposito2/DockerSwarmAkka/releases) for version changes

---

*This documentation covers the complete API surface for version 0.0.10*
