import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("com.ykatsatos.microservices.kotlin-application-conventions")

	//kotlin("plugin.noarg") version "1.9.23"
}

noArg {

	annotations("com.ykatsatos.microservices.core.review.persistence.NoArg")
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
		attributes("Start-Class" to "com.ykatsatos.microservices.core.review.ReviewServiceApplicationKt")
	}
}