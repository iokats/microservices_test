import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("se.magnus.microservices.kotlin-application-conventions")
}

dependencies {

	implementation(project(":api"))
	implementation(project(":utilities"))
	implementation(libs.mapstruct)
	implementation(libs.mapstruct.spring.extension)
	kapt(libs.mapstruct.processor)
	implementation(libs.spring.data.jpa)
	implementation(libs.mysql.connector.j)
	implementation(libs.testcontainers.bom)
	testImplementation(libs.testcontainers)
	testImplementation(libs.testcontainers.junit.jupiter)
	testImplementation(libs.testcontainers.mysql)
}

tasks.named<BootJar>("bootJar") {
	manifest {
		attributes("Start-Class" to "se.magnus.microservices.core.review.ReviewServiceApplicationKt")
	}
}