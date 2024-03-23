import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.ykatsatos.microservices.kotlin-application-conventions")
}

dependencies {

    implementation(libs.springdoc.openapi.common)
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}