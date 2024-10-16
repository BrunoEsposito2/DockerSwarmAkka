import java.io.ByteArrayOutputStream

tasks.register("setupAndDeploy") {
    doFirst {
        var joinToken = ""
        var managerIp = ""

        // Verifica se lo swarm è già inizializzato
        val swarmStatus = ByteArrayOutputStream()
        exec {
            commandLine("docker", "info", "--format", "{{.Swarm.LocalNodeState}}")
            standardOutput = swarmStatus
        }

        if (swarmStatus.toString().trim() != "active") {
            // Inizializza lo swarm se non è attivo
            exec {
                commandLine("docker", "swarm", "init")
            }
        } else {
            exec {
                commandLine("docker", "swarm", "leave", "--force")
            }
            Thread.sleep(4000)
            exec {
                commandLine("docker", "swarm", "init")
            }
            println("Swarm is already active.")
        }

        // Ottieni il token di join
        val tokenOutput = ByteArrayOutputStream()
        exec {
            commandLine("docker", "swarm", "join-token", "-q", "worker")
            standardOutput = tokenOutput
        }
        joinToken = tokenOutput.toString().trim()

        // Ottieni l'IP del manager
        val ipOutput = ByteArrayOutputStream()
        exec {
            commandLine("docker", "node", "inspect", "--format", "{{.Status.Addr}}", "self")
            standardOutput = ipOutput
        }
        managerIp = ipOutput.toString().trim()

        // Crea la rete overlay se non esiste già
        exec {
            commandLine("docker", "network", "create", "--driver", "overlay", "--attachable", "swarm-network")
            isIgnoreExitValue = true
        }

        // Distribuisci lo stack usando Docker Compose
        exec {
            environment("JOIN_TOKEN", joinToken)
            environment("MANAGER_IP", managerIp)
            commandLine("docker-compose", "up", "-d")
        }

        println("Stack deployed successfully. Worker nodes should now be joining the swarm.")

        // Attendi che i nodi si uniscano allo swarm
        Thread.sleep(15000)

        exec {
            commandLine("docker", "stack", "deploy", "-c", "portainer.yml", "portainer")
        }

        // Verifica lo stato dei nodi
        exec {
            commandLine("docker", "node", "ls")
        }
    }
}