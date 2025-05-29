---
layout: default
title: API Documentation v0
---

# Public API Documentation v0

**Current Version:** 0.0.10  
**Generated:** 2025-05-29 14:16:29 UTC

## 📨 Message Protocol API

- [Scala API Documentation](./scala/) - Actor message protocols and service keys

## 🐳 Deployment API

- [Docker Compose Interface](./docker/) - Service configuration and networking

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

### Using the Message Protocol

```scala
import org.example.api.protocol.{Ping, Pong}
import org.example.api.discovery.ServiceKeys

// Register service
context.system.receptionist ! Receptionist.Register(ServiceKeys.PingServiceKey, context.self)

// Send message
pingService ! Ping(context.self)
```

### Deploying with Docker Compose

```bash
# Set required environment variables
export JOIN_TOKEN=$(docker swarm join-token -q worker)
export MANAGER_IP=$(docker node inspect self --format '{{.Status.Addr}}')

# Deploy the cluster
docker-compose up -d
```
