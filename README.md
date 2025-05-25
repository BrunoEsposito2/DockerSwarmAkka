# Akka Cluster Docker Swarm 

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (JDK)
- **Docker** and **Docker Compose**
- **Git**

### Setup Development Environment
```bash
# First time setup
./gradlew devSetup

# Check project status
./gradlew projectStatus
```

### Build and Test
```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Validate API compliance
./gradlew apiValidate
```

### Deploy to Docker Swarm
```bash
# Initialize and deploy
./gradlew deploySwarm

# Check deployment status
./gradlew projectStatus
```

## 🛠️ Available Commands

Run `./gradlew projectHelp` to see all available commands with examples.

### Development
- `./gradlew devSetup` - Setup development environment
- `./gradlew build` - Build the project
- `./gradlew test` - Run tests
- `./gradlew projectStatus` - Show project status

### Validation
- `./gradlew apiValidate` - Validate API compliance
- `./gradlew check` - Run all verification tasks

### Documentation
- `./gradlew apiDocs` - Generate API documentation

### Docker & Deployment
- `./gradlew initSwarm` - Initialize Docker Swarm
- `./gradlew deploySwarm` - Deploy to Docker Swarm

### Cleanup
- `./gradlew clean` - Clean build artifacts
- `./gradlew cleanAll` - Clean all artifacts including Docker

## 🏗️ Project Structure

```
├── node1/                  # Worker 1 (Ping sender)
├── node2/                  # Worker 2 (Pong sender)  
├── docs/
│   ├── PUBLIC-API.md      # Public API specification
│   └── api/generated/     # Auto-generated documentation
├── docker-compose.yml     # Service definitions
├── deploy-akka.sh        # Deployment script
└── build.gradle.kts      # Build configuration & tasks
```

## 🔄 Development Workflow

### 1. Initial Setup
```bash
./gradlew devSetup
./gradlew apiValidate
```

### 2. Development Cycle
```bash
# Make changes to code
./gradlew build test
./gradlew apiValidate
```

### 3. Deployment
```bash
./gradlew clean build deploySwarm
```

### 4. Documentation
```bash
./gradlew apiDocs
```

## 🎯 API Compliance

This project follows **Semantic Versioning (SemVer)** principles with automated API validation:

- **Message Protocol**: Ping/Pong messages with service discovery
- **Docker Services**: worker1 (port 2555), worker2 (port 2551)
- **Configuration**: Akka cluster with seed nodes
- **Deployment**: Docker Swarm with overlay networking

Run `./gradlew apiValidate` to check compliance.

## 🐳 Docker Swarm Architecture

- **Manager Node**: Initializes swarm and runs Portainer
- **Worker Nodes**: Run Akka cluster members
- **Overlay Network**: `swarm-network` for inter-service communication
- **Service Discovery**: Akka receptionist pattern

## 📚 Documentation

- `docs/PUBLIC-API.md` - Public API specification
- `docs/api/generated/` - Auto-generated documentation
- Run `./gradlew apiDocs` to regenerate documentation

## 🔍 Monitoring

### Check Cluster Status
```bash
# Project overview
./gradlew projectStatus

# Docker-specific status
docker node ls
docker service ls
```

### View Logs
```bash
# Application logs
docker service logs akka_worker1 -f
docker service logs akka_worker2 -f

# All services
docker-compose logs -f
```

## 🎓 University Presentation

Complete setup for demonstration:
```bash
./gradlew devSetup build apiValidate deploySwarm apiDocs
```

This will:
1. Setup development environment
2. Build and validate the project
3. Deploy to Docker Swarm
4. Generate documentation

## 🔧 Troubleshooting

### Windows Users
- Use `gradlew.bat` instead of `./gradlew`
- Ensure Docker Desktop is running
- Use PowerShell or Command Prompt

### Common Issues

**Docker Swarm not initialized:**
```bash
./gradlew initSwarm
```

**Build failures:**
```bash
./gradlew clean build
```

**API validation errors:**
```bash
./gradlew apiValidate
# Check output for specific issues
```

**Port conflicts:**
```bash
docker-compose down
./gradlew cleanAll
./gradlew deploySwarm
```

## 📋 Technical Details

- **Akka Version**: 2.8.6
- **Scala Version**: 3.3.3
- **Java Version**: 17
- **Gradle Version**: 8.7
- **Docker Compose Version**: 3.8

## 🏷️ Versioning

This project uses **Semantic Versioning** with automated releases:
- **PATCH**: Bug fixes and internal changes
- **MINOR**: New features (backward compatible)
- **MAJOR**: Breaking changes

Version 0.x.x indicates initial development where API may change.

## 📝 Contributing

1. Follow conventional commit format: `type: description`
2. Run `./gradlew apiValidate` before committing
3. Ensure all tests pass: `./gradlew test`
4. Update documentation if needed: `./gradlew apiDocs`

## 📄 License

University project for Software Process Engineering course.
