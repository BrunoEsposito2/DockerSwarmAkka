# Node2 (Secondary) - Scala API Documentation

**Version:** 0.0.10  
**Node Role:** Secondary cluster node (Pong service provider)  
**Port:** 2551

## Overview

This documentation covers the Node2-specific implementation of the Akka cluster.
Node2 serves as a secondary cluster node and implements the Pong service.

## Key Files

- **App.scala** - Secondary node application logic
- **WorkerOne.scala** - Secondary worker actor implementation
- **Public API** - Shared message protocol and service keys

## Browse Documentation

- [Browse Scaladoc](./index.html) - Complete Node2 API reference
- [Package Index](./package.html) - Package overview
- [Back to Overview](../index.html) - Return to main Scala API docs

## Node2 Responsibilities

- Join Docker Swarm as worker node
- Provide Pong service to the cluster
- Respond to ping messages from Node1
- Handle secondary actor system configuration
