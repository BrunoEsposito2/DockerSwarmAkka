# PUBLIC-API.md

# Akka Cluster Docker Swarm - Public API

## 📋 Overview

This document defines the public API for our Akka Cluster implementation with Docker Swarm orchestration. Following Semantic Versioning (SemVer) principles, this API specification ensures compatibility and proper version management.

## 🔌 Public API Components

### 1. Message Protocol API

```scala
// STABLE: Core message types
trait Message
final case class Ping(replyTo: ActorRef[Pong]) extends Message
final case class Pong(replyTo: ActorRef[Ping]) extends Message

// STABLE: Service discovery keys
val PingServiceKey = ServiceKey[Message]("pingService")
val PongServiceKey = ServiceKey[Message]("pongService")
```

**Usage Example:**
```scala
// Register service
context.system.receptionist ! Receptionist.Register(PingServiceKey, context.self)

// Send message
replyTo ! Pong(context.self)
```

### 2. Docker Deployment API

```yaml
# STABLE: Service definitions
services:
  worker1:
    image: brunoesposito2/akka-node1:${VERSION}
    ports: ["2555:2555"]
    environment:
      - JOIN_TOKEN=${JOIN_TOKEN}
      - MANAGER_IP=${MANAGER_IP}
    
  worker2:
    image: brunoesposito2/akka-node2:${VERSION}  
    ports: ["2551:2551"]
    environment:
      - JOIN_TOKEN=${JOIN_TOKEN}
      - MANAGER_IP=${MANAGER_IP}

# STABLE: Network configuration
networks:
  swarm-network:
    driver: overlay
    attachable: true
```

### 3. Configuration API

```hocon
# STABLE: Akka cluster configuration
akka {
  cluster {
    seed-nodes = [
      "akka://akka-cluster-system@worker1:2555",
      "akka://akka-cluster-system@worker2:2551"
    ]
    downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"
  }
  
  remote.artery {
    canonical {
      hostname = "worker1"  # Node-specific
      port = 2555          # Matches Docker port
    }
    transport = tcp
  }
  
  actor {
    provider = "cluster"
    allow-java-serialization = on
  }
}
```

### 4. Build & Deployment API

```bash
# STABLE: Gradle tasks
./gradlew test              # Run tests
./gradlew build             # Build project
./gradlew apiValidate       # Validate API compliance
./gradlew deploySwarm       # Deploy to Docker Swarm
./gradlew apiDocs           # Generate documentation

# STABLE: Development workflow
./gradlew devSetup          # Setup development environment
./gradlew projectStatus     # Check project status
./gradlew cleanAll          # Clean all artifacts
```

## 📊 Versioning Rules (SemVer)

### MAJOR Version (X.0.0) - Breaking Changes
- ❌ Modify existing message case classes
- ❌ Remove Docker services or change ports
- ❌ Remove required configuration properties
- ❌ Change service key names

### MINOR Version (0.X.0) - New Features
- ✅ Add new message types
- ✅ Add new Docker services
- ✅ Add optional configuration properties
- ✅ Add new Gradle tasks

### PATCH Version (0.0.X) - Bug Fixes
- ✅ Fix bugs without API changes
- ✅ Update documentation
- ✅ Internal refactoring
- ✅ Dependency updates

## 🛠️ Development Workflow

### 1. Setup Development Environment
```bash
./gradlew devSetup    # Create directories and check dependencies
./gradlew projectStatus    # Check project status
```

### 2. Before Making Changes
```bash
./gradlew apiValidate    # Validate current API compliance
./gradlew projectStatus  # Check project status
```

### 3. During Development
```bash
./gradlew build        # Build project
./gradlew test         # Run tests
./gradlew apiValidate  # Check API compliance
```

### 4. Commit with Conventional Format
```bash
# For new features
git commit -m "feat: add health monitoring endpoint"

# For bug fixes  
git commit -m "fix: resolve cluster connection timeout"

# For breaking changes
git commit -m "feat!: change message protocol to protobuf

BREAKING CHANGE: Message format changed from case classes to protobuf.
Migration guide available in docs/migration.md"
```

## 🔍 API Validation

### Automated Validation
```bash
# Run full API validation
./gradlew apiValidate

# Check project status (includes API info)
./gradlew projectStatus

# Generate API documentation
./gradlew apiDocs
```

## 🤖 API Automatic Generation
### Automatic Triggers (CI/CD)
Documentation is automatically updated when you:
- ✅ Modify any .scala file (message protocols)
- ✅ Update docker-compose.yml (service definitions)
- ✅ Change any application.conf file (configuration)
- ✅ Push to main or dev branches
- ✅ Create a Pull Request

### Manual Generation (Local)
You can also generate documentation manually:
```bash
# Generate all documentation
./gradlew apiDocs
```

## 📚 Project Structure

```
├── docs/
│   ├── PUBLIC-API.md          # This file
│   └── api/generated/         # Auto-generated docs
├── node1/                     # Worker 1 implementation
├── node2/                     # Worker 2 implementation  
├── docker-compose.yml         # Service definitions
├── deploy-akka.sh            # Deployment script
├── build.gradle.kts          # Build configuration & tasks
└── .github/workflows/        # CI/CD pipelines
```

## 🚀 Deployment Guide

### Local Development
```bash
# Setup and build
./gradlew devSetup build

# Start services locally
docker-compose up --build

# Check cluster status
./gradlew projectStatus
```

### Production Deployment
```bash
# Deploy to Docker Swarm
./gradlew deploySwarm

# Or use deployment script
./deploy-akka.sh

# Check deployment status
./gradlew projectStatus
```

### CI/CD Pipeline
1. **Commit** with conventional message format
2. **Automated validation** runs on PR (`./gradlew apiValidate`)
3. **Semantic versioning** determines version bump
4. **Docker images** built and pushed automatically
5. **GitHub release** created with changelog

## 🔧 Troubleshooting

### Common Issues

1. **API Validation Fails**
   ```bash
   # Check what's wrong
   ./gradlew apiValidate
   
   # Check overall project status
   ./gradlew projectStatus
   ```

2. **Docker Issues**
   ```bash
   # Validate Docker Compose
   docker-compose config
   
   # Initialize Swarm
   ./gradlew initSwarm
   ```

3. **Build Issues**
   ```bash
   # Clean and rebuild
   ./gradlew cleanAll
   ./gradlew build
   ```

4. **Windows-Specific Issues**
   ```bash
   # Use Windows Gradle wrapper
   gradlew.bat devSetup
   gradlew.bat apiValidate
   
   # Ensure Docker Desktop is running
   ```

## 📞 Resources

- **Akka Documentation**: https://doc.akka.io/
- **Docker Documentation**: https://docs.docker.com/
- **Semantic Versioning**: https://semver.org/
- **Gradle Documentation**: https://docs.gradle.org/

## 🎓 University Context

This project demonstrates:
- **Build Automation**: Gradle-based task system
- **API Design**: Well-defined public interfaces
- **Containerization**: Docker Swarm orchestration  
- **Process Engineering**: Automated validation and documentation
- **Version Management**: Semantic versioning with conventional commits