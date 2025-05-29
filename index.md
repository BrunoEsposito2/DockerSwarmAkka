---
layout: default
title: Home
---

# Akka Cluster Docker Swarm Project

A university project demonstrating **Akka clustering** with **Docker Swarm orchestration**, featuring automated deployment, semantic versioning, and comprehensive API documentation.

## 🚀 Quick Start

Deploy the cluster in minutes:

```bash
git clone https://github.com/brunoesposito2/DockerSwarmAkka.git
cd DockerSwarmAkka
docker-compose up -d
```

Or use the automated deployment script:

```bash
curl -sSL https://github.com/brunoesposito2/DockerSwarmAkka/releases/latest/download/deploy-akka.sh -o deploy-akka.sh
chmod +x deploy-akka.sh
./deploy-akka.sh
```

## 📚 Documentation

### Public API Documentation

- **[Message Protocol API](./api/)** - Scala actor messages and service discovery
- **[Deployment API](./api/)** - Docker Compose interface and networking

### Project Guides

- **[Quick Start Guide](#quick-start)** - Get up and running
- **[Deployment Guide](https://github.com/brunoesposito2/DockerSwarmAkka#deployment)** - Production deployment
- **[API Migration Guide](https://github.com/brunoesposito2/DockerSwarmAkka/releases)** - Version compatibility

## 🏗️ Architecture

This project demonstrates:

- **Akka Actor Model** with typed actors and clustering
- **Docker Swarm** for container orchestration  
- **Semantic Versioning** for API stability
- **CI/CD Pipeline** with automated testing and deployment
- **Public API Design** with comprehensive documentation

### System Components

| Component | Description | Port |
|-----------|-------------|------|
| **worker1** | Primary Akka cluster node | 2555 |
| **worker2** | Secondary Akka cluster node | 2551 |
| **swarm-network** | Overlay network for inter-service communication | - |

## 🔄 Message Protocol

The cluster uses a simple ping-pong protocol for demonstration:

```scala
import org.example.api.protocol.{Ping, Pong}
import org.example.api.discovery.ServiceKeys

// Register service
context.system.receptionist ! Receptionist.Register(
  ServiceKeys.PingServiceKey, 
  context.self
)

// Send message
pingService ! Ping(context.self)
```

## 🐳 Docker Deployment

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `JOIN_TOKEN` | Docker Swarm worker join token | `SWMTKN-1-...` |
| `MANAGER_IP` | Swarm manager IP address | `192.168.1.100` |

### Services Configuration

The deployment uses Docker Compose with the following public interface:

- **Service Names**: `worker1`, `worker2` (part of public API)
- **Network**: `swarm-network` (overlay, attachable)
- **Ports**: 2555 (worker1), 2551 (worker2)

## 📋 API Versioning

This project follows [Semantic Versioning 2.0.0](https://semver.org/):

- **MAJOR**: Breaking changes (message protocol, port changes, service renames)
- **MINOR**: Backward compatible features (new message types, optional configurations)  
- **PATCH**: Backward compatible fixes (bug fixes, documentation updates)

## 🎓 Academic Context

### Learning Objectives

- **Distributed Systems**: Understanding clustering and fault tolerance
- **Container Orchestration**: Docker Swarm deployment patterns
- **API Design**: Public interface design and versioning
- **DevOps**: CI/CD, automated testing, and deployment automation

### Technologies Demonstrated

- **Akka**: Actor model, clustering, service discovery
- **Scala**: Functional programming, type safety
- **Docker**: Containerization, multi-stage builds
- **Docker Swarm**: Container orchestration, overlay networking
- **GitHub Actions**: CI/CD, automated releases, documentation generation

## 🔗 Links

- **[GitHub Repository](https://github.com/brunoesposito2/DockerSwarmAkka)** - Source code and issues
- **[Releases](https://github.com/brunoesposito2/DockerSwarmAkka/releases)** - Version history and downloads
- **[Docker Images](https://hub.docker.com/u/brunoesposito2)** - Published container images
- **[API Documentation](./api/)** - Complete API reference

---

**Built with ❤️ for academic demonstration of modern distributed systems and DevOps practices.**