import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("se.magnus.microservices.kotlin-application-conventions")
}

dependencies {

	implementation(project(":api"))
	implementation(project(":utilities"))
	implementation(libs.springdoc.openapi.webflux)
}

tasks.named<BootJar>("bootJar") {
	manifest {
		attributes("Start-Class" to "se.magnus.microservices.composite.product.ProductCompositeServiceApplicationKt")
	}
}