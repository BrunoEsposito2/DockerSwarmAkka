---
layout: default
title: REST API Guide v0
---

# REST API Documentation v0

**Version:** 0.0.10  
**Generated:** 2025-05-29 22:04:15 UTC

## Interactive Documentation

- [**Swagger UI Interface**](./index.html) - Interactive API documentation
- [**Download OpenAPI 3.0 Spec**](./openapi.json) - Latest OpenAPI specification  
- [**Download Swagger 2.0 Spec**](./swagger.json) - Legacy Swagger specification

## Quick Links

- 🚀 [**Test API Live**](./index.html) - Try endpoints directly in browser
- 📋 [**API Reference**](./index.html#/operations-tag-Camera_Management) - Complete endpoint list
- 🔍 [**Status Monitoring**](./index.html#/operations-tag-Monitoring) - System health endpoints

## API Overview

The Akka Cluster Camera Management REST API provides HTTP endpoints for:

- **Camera Management**: Switch between available cameras
- **Detection Configuration**: Set detection window coordinates  
- **System Monitoring**: Real-time status and detection data

## Base URLs

| Environment | URL | Description |
|-------------|-----|-------------|
| **Development** | http://localhost:4000 | Local development server |
| **Node1 (Primary)** | http://worker1:4000 | Production primary node |
| **Node2 (Secondary)** | http://worker2:4000 | Production secondary node |

---

*For complete interactive testing, use the [Swagger UI interface](./index.html) above.*
