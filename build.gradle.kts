import org.gradle.internal.impldep.org.testng.Assert.assertTrue
import java.io.ByteArrayOutputStream

tasks.register("initSwarm") {
    group = "docker"
    description = "Initialize Docker Swarm"
    
    doFirst {
        // Verifica se lo swarm è già inizializzato
        val swarmStatus = ByteArrayOutputStream()
        exec {
            commandLine("docker", "info", "--format", "{{.Swarm.LocalNodeState}}")
            standardOutput = swarmStatus
            isIgnoreExitValue = true
        }

        if (swarmStatus.toString().trim() != "active") {
            println("Initiating swarm...")
            // Inizializza lo swarm se non è attivo
            exec {
                commandLine("docker", "swarm", "init")
            }
        } else {
            println("Swarm is already active. Ongoing leave process...")
            exec {
                commandLine("docker", "swarm", "leave", "--force")
                isIgnoreExitValue = true
            }
            Thread.sleep(4000)
            println("Initiating swarm...")
            exec {
                commandLine("docker", "swarm", "init")
            }
        }
    }
}

tasks.register("deploySwarm") {
    group = "deployment"
    description = "Deploy to Docker Swarm"
    dependsOn("initSwarm")
    
    doFirst {
        var joinToken = ""
        var managerIp = ""

        // Get the join token
        val tokenOutput = ByteArrayOutputStream()
        exec {
            commandLine("docker", "swarm", "join-token", "-q", "worker")
            standardOutput = tokenOutput
        }
        joinToken = tokenOutput.toString().trim()

        // Get the IP of the manager
        val ipOutput = ByteArrayOutputStream()
        exec {
            commandLine("docker", "node", "inspect", "--format", "{{.Status.Addr}}", "self")
            standardOutput = ipOutput
        }
        managerIp = ipOutput.toString().trim()

        // Create the overlay network if it does not already exist
        exec {
            commandLine("docker", "network", "create", "--driver", "overlay", "--attachable", "swarm-network")
            isIgnoreExitValue = true
        }

        // Deploy the stack using Docker Compose
        exec {
            environment("JOIN_TOKEN", joinToken)
            environment("MANAGER_IP", managerIp)
            commandLine("docker-compose", "up", "--build", "-d")
        }

        println("Stack deployed successfully. Worker nodes should now be joining the swarm.")
    }
}

// Task per la validazione API
tasks.register("apiValidate") {
    group = "verification"
    description = "Validate API compliance"
    
    doLast {
        println(" Validating API compliance...")
        
        var validationErrors = 0
        
        fun reportError(message: String) {
            println(" $message")
            validationErrors++
        }
        
        fun reportSuccess(message: String) {
            println(" $message")
        }
        
        fun reportWarning(message: String) {
            println(" $message")
        }
        
        // 1. Validate Message Protocol API
        println("\n 1. Validating Message Protocol API...")
        
        val scalaFiles = fileTree(".").matching { 
            include("**/*.scala")
            exclude("**/build/**")
        }
        
        val hasPing = scalaFiles.any { file ->
            file.readText().contains("case class Ping") && file.readText().contains("extends Message")
        }
        
        val hasPong = scalaFiles.any { file ->
            file.readText().contains("case class Pong") && file.readText().contains("extends Message")
        }
        
        val hasPingServiceKey = scalaFiles.any { file ->
            file.readText().contains("PingServiceKey") && file.readText().contains("ServiceKey")
        }
        
        val hasPongServiceKey = scalaFiles.any { file ->
            file.readText().contains("PongServiceKey") && file.readText().contains("ServiceKey")
        }
        
        if (hasPing) reportSuccess("Ping message type found") else reportError("Required Ping message type not found")
        if (hasPong) reportSuccess("Pong message type found") else reportError("Required Pong message type not found")
        if (hasPingServiceKey) reportSuccess("PingServiceKey found") else reportError("Required PingServiceKey not found")
        if (hasPongServiceKey) reportSuccess("PongServiceKey found") else reportError("Required PongServiceKey not found")
        
        // 2. Validate Docker Compose API
        println("\n 2. Validating Docker Compose API...")
        
        val dockerComposeFile = file("docker-compose.yml")
        if (!dockerComposeFile.exists()) {
            reportError("docker-compose.yml not found")
        } else {
            val content = dockerComposeFile.readText()
            
            if (content.contains("worker1:")) reportSuccess("Service 'worker1' found") else reportError("Required service 'worker1' not found")
            if (content.contains("worker2:")) reportSuccess("Service 'worker2' found") else reportError("Required service 'worker2' not found")
            if (content.contains("2555:2555")) reportSuccess("Port 2555:2555 mapping found") else reportError("Required port mapping 2555:2555 not found")
            if (content.contains("2551:2551")) reportSuccess("Port 2551:2551 mapping found") else reportError("Required port mapping 2551:2551 not found")
            if (content.contains("JOIN_TOKEN")) reportSuccess("JOIN_TOKEN environment variable configured") else reportError("Required JOIN_TOKEN environment variable not found")
            if (content.contains("MANAGER_IP")) reportSuccess("MANAGER_IP environment variable configured") else reportError("Required MANAGER_IP environment variable not found")
            if (content.contains("swarm-network")) reportSuccess("swarm-network found") else reportError("Required swarm-network not found")
        }
        
        // 3. Validate Build System
        println("\n 3. Validating Build System...")
        
        if (file("gradlew").exists() || file("gradlew.bat").exists()) {
            reportSuccess("Gradle wrapper found")
        } else {
            reportError("Gradle wrapper not found")
        }
        
        if (file("build.gradle.kts").exists() || file("build.gradle").exists()) {
            reportSuccess("Main build file found")
        } else {
            reportError("Main build file not found")
        }
        
        // Check node-specific build files
        listOf("node1", "node2").forEach { node ->
            if (file("$node/build.gradle.kts").exists() || file("$node/build.gradle").exists()) {
                reportSuccess("Build file found for $node")
            } else {
                reportError("Build file not found for $node")
            }
        }
        
        // 4. Check API Stability
        println("\n 4. Checking API Stability...")
        
        // Get current version
        val currentVersion = try {
            val versionOutput = ByteArrayOutputStream()
            exec {
                commandLine("git", "describe", "--tags", "--abbrev=0")
                standardOutput = versionOutput
                isIgnoreExitValue = true
            }
            versionOutput.toString().trim().removePrefix("v").ifEmpty { "0.1.0" }
        } catch (e: Exception) {
            "0.1.0"
        }
        
        println("Current version: $currentVersion")
        
        if (currentVersion.startsWith("0.")) {
            reportWarning("Version 0.x.x - API may change without notice (initial development)")
            println("   In initial development, breaking changes are expected")
        } else {
            // Check if PUBLIC-API.md exists for stable versions
            if (file("docs/PUBLIC-API.md").exists()) {
                reportSuccess("PUBLIC-API.md found for stable version")
            } else {
                reportError("PUBLIC-API.md not found - required for stable versions")
            }
        }
        
        // Final Report
        println("\n === Validation Summary ===")
        
        if (validationErrors == 0) {
            println(" All critical API validations passed!")
            println(" The project appears to comply with the defined public API.")
            println("\n Current Version Information:")
            println("Version: $currentVersion")
            if (currentVersion.startsWith("0.")) {
                println("Status: Initial Development - API may change")
                println("Next stable: Consider releasing 1.0.0 when API is stable")
            } else {
                println("Status: Stable API - SemVer rules strictly applied")
            }
        } else {
            println(" Found $validationErrors API compliance issues.")
            println(" Please fix these issues to ensure API stability.")
            println("\n Common fixes:")
            println("- Ensure all required Scala message types are present")
            println("- Verify docker-compose.yml has required services and ports")
            println("- Check Akka configuration files for required settings")
            println("- Update PUBLIC-API.md when making API changes in stable versions")
            throw GradleException("API validation failed with $validationErrors errors")
        }
    }
}

// Task per il setup dell'ambiente di sviluppo
tasks.register("devSetup") {
    group = "setup"
    description = "Setup development environment"
    
    doLast {
        println(" Setting up development environment...")
        
        // Crea directory necessarie
        listOf("docs/api/generated", "scripts", "logs").forEach { dir ->
            file(dir).mkdirs()
            println(" Created directory: $dir")
        }
        
        // Controlla dipendenze
        println("\n Checking dependencies...")
        
        val dependencies = mapOf(
            "java" to "Java (JDK 17 required)",
            "docker" to "Docker",
            "docker-compose" to "Docker Compose", 
            "git" to "Git"
        )
        
        dependencies.forEach { (command, description) ->
            try {
                exec {
                    commandLine(command, "--version")
                    standardOutput = ByteArrayOutputStream()
                }
                println(" $description found")
            } catch (e: Exception) {
                println(" $description not found")
            }
        }
        
        // Imposta permessi (solo su sistemi Unix)
        val os = System.getProperty("os.name").lowercase()
        if (!os.contains("win")) {
            try {
                exec {
                    commandLine("chmod", "+x", "gradlew")
                    isIgnoreExitValue = true
                }
                if (file("deploy-akka.sh").exists()) {
                    exec {
                        commandLine("chmod", "+x", "deploy-akka.sh")
                        isIgnoreExitValue = true
                    }
                }
                println(" Permissions set for Unix scripts")
            } catch (e: Exception) {
                println(" Could not set permissions (this is normal on Windows)")
            }
        }
        
        println("\n Development environment setup complete")
        println("Use './gradlew tasks' to see available tasks")
    }
}

// Task per il controllo dello stato del progetto
tasks.register("projectStatus") {
    group = "help"
    description = "Show project status"
    
    doLast {
        println("Project Status Report")
        println("=======================")
        
        // Informazioni Git
        println("Git Status:")
        try {
            val gitStatus = ByteArrayOutputStream()
            exec {
                commandLine("git", "status", "--porcelain")
                standardOutput = gitStatus
                isIgnoreExitValue = true
            }
            
            val statusLines = gitStatus.toString().trim().split("\n").take(10)
            if (statusLines.any { it.isNotBlank() }) {
                statusLines.forEach { if (it.isNotBlank()) println("  $it") }
            } else {
                println("  Working directory clean")
            }
        } catch (e: Exception) {
            println("  Git not available or not a git repository")
        }
        
        println()
        
        // Informazioni versione
        println("Version Info:")
        try {
            val versionOutput = ByteArrayOutputStream()
            exec {
                commandLine("git", "describe", "--tags", "--abbrev=0")
                standardOutput = versionOutput
                isIgnoreExitValue = true
            }
            
            val version = versionOutput.toString().trim()
            if (version.isNotEmpty()) {
                println("  Current version: $version")
            } else {
                println("  No tags found - version 0.1.0")
            }
            
            val commitOutput = ByteArrayOutputStream()
            exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = commitOutput
                isIgnoreExitValue = true
            }
            println("  Latest commit: ${commitOutput.toString().trim()}")
            
            val branchOutput = ByteArrayOutputStream()
            exec {
                commandLine("git", "branch", "--show-current")
                standardOutput = branchOutput
                isIgnoreExitValue = true
            }
            println("  Branch: ${branchOutput.toString().trim()}")
            
        } catch (e: Exception) {
            println("  Git information not available")
        }
        
        println()
        
        // Status build
        println("Build Status:")
        val buildDirs = listOf("build", "node1/build", "node2/build")
        val hasBuilds = buildDirs.any { file(it).exists() }
        
        if (hasBuilds) {
            println("  Build artifacts present")
        } else {
            println("  No build artifacts found - run './gradlew build'")
        }
        
        println()
        
        // Status Docker
        println("Docker Status:")
        try {
            exec {
                commandLine("docker", "info")
                standardOutput = ByteArrayOutputStream()
            }
            
            val swarmOutput = ByteArrayOutputStream()
            exec {
                commandLine("docker", "info", "--format", "{{.Swarm.LocalNodeState}}")
                standardOutput = swarmOutput
                isIgnoreExitValue = true
            }
            
            val swarmState = swarmOutput.toString().trim()
            when (swarmState) {
                "active" -> {
                    println("  Docker Swarm active")
                    try {
                        exec {
                            commandLine("docker", "node", "ls")
                        }
                    } catch (e: Exception) {
                        // Ignore se non riesce a mostrare i nodi
                    }
                }
                else -> println("  Docker running but Swarm not initialized")
            }
        } catch (e: Exception) {
            println("  Docker not available")
        }
        
        println()
        println("Run './gradlew tasks' for available tasks")
    }
}

// Task per la generazione della documentazione API
tasks.register("apiDocs") {
    group = "documentation"
    description = "Generate API documentation"
    
    doLast {
        println("Generating API documentation...")
        
        val docsDir = file("docs/api/generated")
        docsDir.mkdirs()
        
        // Genera documentazione messaggi
        val messagesDoc = File(docsDir, "messages.md")
        messagesDoc.writeText("""
            # Message Protocol Documentation
            Generated on: ${java.time.LocalDateTime.now()}
            
            ## Available Messages
            
        """.trimIndent())
        
        fileTree(".").matching { 
            include("**/*.scala")
            exclude("**/build/**")
        }.forEach { file ->
            val content = file.readText()
            if (content.contains("case class") && content.contains("extends Message")) {
                messagesDoc.appendText("\n### Messages in ${file.relativeTo(projectDir)}\n")
                content.lines().forEach { line ->
                    if (line.trim().startsWith("case class") && line.contains("Message")) {
                        messagesDoc.appendText("- `${line.trim()}`\n")
                    }
                }
            }
        }
        
        // Genera documentazione Docker
        val dockerDoc = File(docsDir, "docker.md")
        dockerDoc.writeText("""
            # Docker Services Documentation  
            Generated on: ${java.time.LocalDateTime.now()}
            
            ## Services
            
        """.trimIndent())
        
        val dockerComposeFile = file("docker-compose.yml")
        if (dockerComposeFile.exists()) {
            val content = dockerComposeFile.readText()
            val servicePattern = """^\s*([a-zA-Z0-9_-]+):""".toRegex(RegexOption.MULTILINE)
            val services = servicePattern.findAll(content).map { it.groupValues[1] }.toList()
            
            services.forEach { service ->
                dockerDoc.appendText("### Service: $service\n")
                dockerDoc.appendText("- Defined in docker-compose.yml\n")
                
                // Estrai informazioni sulle porte
                val portPattern = """- "(\d+:\d+)\"""".toRegex()
                val ports = portPattern.findAll(content).map { it.groupValues[1] }.toList()
                if (ports.isNotEmpty()) {
                    dockerDoc.appendText("- Ports: ${ports.joinToString(", ")}\n")
                }
                dockerDoc.appendText("\n")
            }
        }
        
        // Genera documentazione configurazione
        val configDoc = File(docsDir, "config.md")
        configDoc.writeText("""
            # Akka Configuration Documentation
            Generated on: ${java.time.LocalDateTime.now()}
            
            ## Configuration Files
            
        """.trimIndent())
        
        fileTree(".").matching {
            include("**/application.conf")
            exclude("**/build/**")
        }.forEach { configFile ->
            configDoc.appendText("\n### Configuration: ${configFile.relativeTo(projectDir)}\n")
            configDoc.appendText("```hocon\n")
            configFile.readText().lines().take(20).forEach { line ->
                configDoc.appendText("$line\n")
            }
            configDoc.appendText("```\n")
        }
        
        println("API documentation generated in ${docsDir.relativeTo(projectDir)}")
    }
}

// Task per cleanup avanzato
tasks.register("cleanAll") {
    group = "cleanup"
    description = "Clean all build artifacts and Docker resources"
    dependsOn("clean")
    
    doLast {
        println("Cleaning Docker resources...")
        
        try {
            exec {
                commandLine("docker", "system", "prune", "-f")
                isIgnoreExitValue = true
            }
            println("Docker cleanup completed")
        } catch (e: Exception) {
            println("Docker cleanup failed or Docker not available")
        }
    }
}

// Task di aiuto personalizzato (RINOMINATO per evitare conflitti)
tasks.register("projectHelp") {
    group = "help"
    description = "Show available project tasks with examples"
    
    doLast {
        println("""
        Akka Cluster University Project
        ==================================

        Available commands:

        Development:
        ./gradlew devSetup          - Setup development environment
        ./gradlew projectStatus     - Show comprehensive project status
        ./gradlew build             - Build the entire project
        ./gradlew test              - Run all tests
        ./gradlew node1:runScalaMain - Run worker1 node
        ./gradlew node2:runScalaMain - Run worker2 node

        Validation:
        ./gradlew apiValidate       - Validate API compliance
        ./gradlew check             - Run all verification tasks

        Documentation:
        ./gradlew apiDocs           - Generate API documentation

        Docker & Deployment:
        ./gradlew initSwarm         - Initialize Docker Swarm
        ./gradlew deploySwarm       - Deploy to Docker Swarm
        
        Cleanup:
        ./gradlew clean             - Clean build artifacts
        ./gradlew cleanAll          - Clean all artifacts including Docker

        Examples:
        ./gradlew devSetup apiValidate build test
        ./gradlew deploySwarm
        ./gradlew projectStatus

        Workflow Examples:
        # First time setup
        ./gradlew devSetup
        
        # Development workflow
        ./gradlew apiValidate build test
        
        # Deployment workflow  
        ./gradlew clean build deploySwarm
        
        # Documentation generation
        ./gradlew apiDocs

        University Presentation:
        ./gradlew devSetup build apiValidate deploySwarm apiDocs

        Tips:
        - Use './gradlew tasks' to see all available Gradle tasks
        - Use './gradlew projectHelp' to see this help again
        - On Windows: Use 'gradlew.bat' instead of './gradlew'
        """.trimIndent())
    }
}

// Integra i task esistenti con quelli dei sottoprogetti
// Solo se il task check esiste (in progetti con plugin java/scala)
tasks.whenTaskAdded {
    if (name == "check") {
        dependsOn("apiValidate")
    }
}

/*tasks.register("deployPortainer") {
    exec {
        commandLine("docker", "stack", "deploy", "-c", "portainer.yml", "portainer")
    }
}*/

/* tasks.register<Test>("testSwarmDeployed") {
    shouldRunAfter("deploySwarm")
    doLast {
        // Ottieni i nomi dei container
        val containerNames = ByteArrayOutputStream()
        exec {
            commandLine("docker-compose", "ps", "--format", "{{.Name}}")
            standardOutput = containerNames
        }

        // Estrai i nomi dall'output
        val namesList = containerNames.toString().trim().split("\n")

        val nodeOutput = ByteArrayOutputStream()
        exec {
            commandLine("docker", "node", "ls")
            standardOutput = nodeOutput
        }
        println("Current swarm nodes: ${nodeOutput.toString().trim()}")

        // Verifica se i nodi sono stati aggiunti correttamente
        assertTrue((namesList.size + 1) == nodeOutput.toString().split("\n").size,
            "Not all nodes joined the current swarm. Please, fix it and try again.")

        println("All tests passed.")
    }
} */