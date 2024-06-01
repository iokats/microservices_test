import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.ykatsatos.microservices.kotlin-application-conventions")
}

dependencies {

    implementation(project(":api"))
    implementation(project(":utilities"))
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation(libs.spring.data.mongodb)
    implementation(libs.mapstruct)
    implementation(libs.mapstruct.spring.extension)
    kapt(libs.mapstruct.processor)
    implementation(libs.testcontainers.bom)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mongodb)
}

tasks.named<BootJar>("bootJar") {
    manifest {
        attributes("Start-Class" to "com.ykatsatos.microservices.core.recommendation.RecommendationServiceApplicationKt")
    }
}