---
layout: default
title: API Documentation v1
---

# Public API Documentation v1

**Current Version:** 1.0.5  
**Generated:** 2025-05-31 14:52:36 UTC

## 📨 Message Protocol API

- [**Scala API Documentation**](./scala/) - Complete documentation for both nodes
  - [Node1 (Primary)](./scala/node1/) - Ping service implementation
  - [Node2 (Secondary)](./scala/node2/) - Pong service implementation

## 🐳 Deployment API

- [**Docker Compose Interface**](./docker/) - Service configuration and networking

## 🌐 REST API

- [**HTTP REST API Guide**](./rest/api-guide.html) - Camera management and monitoring endpoints
  - [Interactive Swagger UI](./rest/index.html) - Test API endpoints directly
  - [OpenAPI 3.0 Specification](./rest/openapi.json) - Latest OpenAPI spec
  - [Swagger 2.0 Specification](./rest/swagger.json) - Legacy Swagger spec

## 🏗️ Architecture Overview

| Node | Role | Port | Service | Documentation |
|------|------|------|---------|---------------|
| **Node1** | Primary | 2555, 4000 | Ping + REST | [Scala](./scala/node1/) \| [REST](./rest/api-guide.html) |
| **Node2** | Secondary | 2551, 4000 | Pong + REST | [Scala](./scala/node2/) \| [REST](./rest/api-guide.html) |

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

*This documentation covers the complete API surface for version 1.0.5*
