---
layout: default
title: Scala API v0
---

# Scala API Documentation v0

**Version:** 0.0.10  
**Generated:** 2025-05-29 15:08:22 UTC

## Documentation Overview

This section contains the complete Scala API documentation for the Akka cluster project, organized by node implementation.

## Available Documentation

### 📦 Shared Public API
- [**Public API Documentation**](./shared/) - Common interfaces and protocols
  - Message protocol definitions (Ping, Pong)
  - Service discovery keys (PingServiceKey, PongServiceKey)
  - Shared interfaces used by both nodes

### 🔧 Node-Specific Implementations
- [**Node1 Documentation**](./node1/) - Primary cluster node implementation
  - Worker1 actor implementation
  - Node1-specific configuration and behavior
  - Ping service implementation
- [**Node2 Documentation**](./node2/) - Secondary cluster node implementation
  - Worker2 actor implementation
  - Node2-specific configuration and behavior
  - Pong service implementation

## Public API Packages

The following packages are part of the **stable public API**:

- **org.example.api.protocol** - Message protocol definitions
- **org.example.api.discovery** - Service discovery keys

## Key Components

### Message Protocol
```scala
// Base message trait
trait Message

// Ping-Pong protocol
case class Ping(replyTo: ActorRef[Pong]) extends Message
case class Pong(replyTo: ActorRef[Ping]) extends Message
```

### Service Discovery
```scala
// Service keys for actor registration
val PingServiceKey: ServiceKey[Message] = ServiceKey[Message]("pingService")
val PongServiceKey: ServiceKey[Message] = ServiceKey[Message]("pongService")
```

## Architecture Overview

### Node1 (Primary)
- **Role**: Ping service provider
- **Port**: 2555
- **Responsibility**: Initiates ping-pong cycles and manages primary cluster coordination

### Node2 (Secondary)  
- **Role**: Pong service provider
- **Port**: 2551
- **Responsibility**: Responds to ping messages and provides secondary cluster services

## Usage Examples

### Registering a Service
```scala
import org.example.api.discovery.ServiceKeys
import akka.actor.typed.receptionist.Receptionist

// Register ping service (Node1)
context.system.receptionist ! Receptionist.Register(
  ServiceKeys.PingServiceKey, 
  context.self
)

// Register pong service (Node2)
context.system.receptionist ! Receptionist.Register(
  ServiceKeys.PongServiceKey, 
  context.self
)
```

### Discovering and Using Services
```scala
import org.example.api.protocol.{Ping, Pong}

// Node1: Send a ping message
pongService ! Ping(context.self)

// Node2: Handle ping and respond with pong
case Ping(replyTo) =>
  println("Ping received")
  replyTo ! Pong(context.self)

// Node1: Handle pong response
case Pong(replyTo) =>
  println("Pong received")
  replyTo ! Ping(context.self)
```

## Navigation Tips

- **For API Users**: Start with [Shared Public API](./shared/) documentation
- **For Implementers**: Review both [Node1](./node1/) and [Node2](./node2/) specific docs
- **For Migration**: Check version-specific changes in each node's documentation

---

*This documentation is automatically generated from the source code and represents both the stable public API and node-specific implementations.*
