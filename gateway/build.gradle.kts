import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {

    id("com.ykatsatos.microservices.kotlin-application-conventions")
}

dependencies {

    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
}

tasks.named<BootJar>("bootJar") {
    manifest {
        attributes("Start-Class" to "com.ykatsatos.springcloud.gateway.GatewayApplicationKt")
    }
}