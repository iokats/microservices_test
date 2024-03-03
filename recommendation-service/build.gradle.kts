import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("se.magnus.microservices.kotlin-application-conventions")
}

dependencies {

    implementation(project(":api"))
    implementation(project(":utilities"))
}

tasks.named<BootJar>("bootJar") {
    manifest {
        attributes("Start-Class" to "se.magnus.microservices.core.recommendation.RecommendationServiceApplicationKt")
    }
}