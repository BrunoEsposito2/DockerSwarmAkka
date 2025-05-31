---
layout: default
title: Scala API v1
---

# Scala API Documentation v1

**Version:** 1.0.6  
**Generated:** 2025-05-31 15:52:51 UTC

## Node Documentation

Complete Scala API documentation for both cluster nodes:

### 🔧 [Node1 Documentation](./node1/)
- **Role**: Primary cluster node (Ping service provider)
- **Port**: 2555
- **Responsibilities**: Cluster coordination, ping initiation
- **Status**: ✅ Documentation available

### 🔧 [Node2 Documentation](./node2/)
- **Role**: Secondary cluster node (Pong service provider)
- **Port**: 2551  
- **Responsibilities**: Message response, worker services
- **Status**: ✅ Documentation available

## Public API Overview

Both nodes share the same public API packages:

- **org.example.api.protocol** - Message protocol definitions (Ping, Pong)
- **org.example.api.discovery** - Service discovery keys (PingServiceKey, PongServiceKey)

## Key Components

### Message Protocol
```scala
trait Message

case class Ping(replyTo: ActorRef[Pong]) extends Message
case class Pong(replyTo: ActorRef[Ping]) extends Message
```

### Service Discovery
```scala
val PingServiceKey: ServiceKey[Message] = ServiceKey[Message]("pingService")
val PongServiceKey: ServiceKey[Message] = ServiceKey[Message]("pongService")
```

## Usage Examples

### Registering Services
```scala
import org.example.api.discovery.ServiceKeys
import akka.actor.typed.receptionist.Receptionist

// Node1: Register ping service
context.system.receptionist ! Receptionist.Register(
  ServiceKeys.PingServiceKey, context.self
)

// Node2: Register pong service  
context.system.receptionist ! Receptionist.Register(
  ServiceKeys.PongServiceKey, context.self
)
```

### Message Exchange
```scala
import org.example.api.protocol.{Ping, Pong}

// Node1: Send ping
pongService ! Ping(context.self)

// Node2: Handle ping, send pong
case Ping(replyTo) =>
  println("Ping received")
  replyTo ! Pong(context.self)

// Node1: Handle pong
case Pong(replyTo) =>
  println("Pong received")  
  replyTo ! Ping(context.self)
```

## Architecture

```
Node1 (Primary)     ←→     Node2 (Secondary)
Port: 2555                 Port: 2551
Service: Ping             Service: Pong
Role: Coordinator         Role: Worker
```

---

*Browse the individual node documentation above for complete API reference and implementation details.*
