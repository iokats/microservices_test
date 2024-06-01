import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.ykatsatos.microservices.kotlin-application-conventions")

    //kotlin("plugin.noarg") version "1.9.23"
}

dependencies {

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
}

tasks.named<BootJar>("bootJar") {
    manifest {
        attributes("Start-Class" to "com.ykatsatos.springcloud.eurekaserver.EurekaServerApplicationKt")
    }
}