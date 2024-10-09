import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

fun getManagerIp(): String {
    var ipAddress = ByteArrayOutputStream()
    exec {
        commandLine("docker", "info", "--format", "'{{.Swarm.NodeAddr}}'")
        isIgnoreExitValue = true
        standardOutput = ipAddress
    }
    return ipAddress.toString().trim().removeSurrounding("'") + ":2377"
}

tasks.register("setupAndDeploy") {
    var joinToken = ""
    doFirst {
        for (i in 1..2) {
            exec {
                commandLine("docker", "run", "-d", "--privileged", "--name", "worker$i", "--hostname=node$i", "docker:dind")
            }
        }

        // Capture the output of the init command
        var initOutput = ByteArrayOutputStream()
        exec {
            commandLine("docker", "swarm", "init")
            isIgnoreExitValue = true
            standardOutput = initOutput
        }
        println("Init process output: " + initOutput.toString())

        // Extract the token from the init output
        val tokenCommand: String = initOutput.toString()
        if (tokenCommand.contains("docker swarm join")) {
            joinToken = tokenCommand.substringAfter("--token ").substringBefore(" ")
        }

        if (joinToken.isEmpty()) {
            throw RuntimeException("Impossible to obtain the join token")
        }
        println("join token: " + joinToken)

        // Create the overlay network
        exec {
            commandLine("docker", "network", "create", "--driver", "overlay", "--attachable", "swarm_network")
        }.apply {
            if (exitValue != 0) {
                throw RuntimeException("Failed to create overlay network")
            }
        }

        // Deploy the stack using Docker Compose
        exec {
            commandLine("docker", "stack", "deploy", "-c", "docker-compose.yml", "worker-stack")
            //commandLine("docker", "compose", "up", "-d")
        }.apply {
            if (exitValue != 0) {
                throw RuntimeException("Failed to deploy the swarm stack")
            }
        }
    }

    doLast {
        println("Aggiunta nodo worker allo Swarm")
        val managerIp = getManagerIp()
        println("Manager IP: $managerIp")

        for (i in 1..2)
            // Join the swarm as a worker node
            exec {
                commandLine("docker", "exec", "worker$i", "docker", "swarm", "join", "--token", joinToken, managerIp)
            }

        println("Setup completato")
    }
}