# Node1 (Primary) - Scala API Documentation

**Version:** 0.0.10  
**Node Role:** Primary cluster node (Ping service provider)  
**Port:** 2555

## Overview

This documentation covers the Node1-specific implementation of the Akka cluster.
Node1 serves as the primary cluster node and implements the Ping service.

## Key Files

- **App.scala** - Main application and cluster coordination
- **WorkerTwo.scala** - Primary worker actor implementation
- **Public API** - Shared message protocol and service keys

## Browse Documentation

- [Browse Scaladoc](./index.html) - Complete Node1 API reference
- [Package Index](./package.html) - Package overview
- [Back to Overview](../index.html) - Return to main Scala API docs

## Node1 Responsibilities

- Initialize Docker Swarm as manager node
- Provide Ping service to the cluster
- Coordinate cluster formation and management
- Handle primary actor system configuration
