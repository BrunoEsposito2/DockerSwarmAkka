/*
 * node1/build.gradle.kts
 */
plugins {
    // Apply the scala Plugin to add support for Scala.
    scala

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    //implementation(project(":common"))

    // https://mvnrepository.com/artifact/org.scala-lang/scala3-library
    implementation("org.scala-lang:scala3-library_3:3.3.3")

    implementation("io.reactivex:rxscala_2.13:0.27.0")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:33.2.1-jre")

    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor-typed
    implementation("com.typesafe.akka:akka-actor-typed_3:2.8.6")

    implementation("com.typesafe.akka:akka-cluster-typed_3:2.8.6")

    // Use Scalatest for testing our library

    // Use Scala 3.1 in our library project
    testImplementation("org.scalatest:scalatest_3:3.2.19")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    testRuntimeOnly("org.scalatestplus:junit-5-10_3:3.2.19.0")

    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor-testkit-typed
    testImplementation("com.typesafe.akka:akka-actor-testkit-typed_3:2.8.6")
    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-slf4j
    implementation("com.typesafe.akka:akka-slf4j_3:2.8.6")

    /* for logging */
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:slf4j-jdk14:2.0.7") 

    implementation("io.vertx:vertx-core:4.4.4")
    implementation("io.vertx:vertx-web:4.4.4")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.15")
    implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
}

tasks {
    test{
        useJUnitPlatform {
            includeEngines("scalatest")
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("org.example.App")
}

tasks.register("runScalaMain") {
    dependsOn("compileScala")
    doLast {
        javaexec {
            mainClass.set(application.mainClass.get())
            classpath = sourceSets.main.get().runtimeClasspath
        }
    }
}

// Custom Scaladoc task for Public API documentation
// Placeholder Scaladoc task (will work in CI with proper scaladoc)
tasks.named("scaladoc") {
    group = "documentation"
    description = "Generate Scaladoc for Public API"
    
    dependsOn("compileScala")
    
    doLast {
        val outputDir = file("$buildDir/docs/scaladoc")
        outputDir.mkdirs()
    }
}