import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("se.magnus.microservices.kotlin-application-conventions")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}