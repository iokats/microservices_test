import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("com.ykatsatos.microservices.kotlin-application-conventions")
}

dependencies {

	implementation(project(":api"))
	implementation(project(":utilities"))
	implementation(libs.springdoc.openapi.webflux)
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
}

tasks.named<BootJar>("bootJar") {
	manifest {
		attributes("Start-Class" to "com.ykatsatos.microservices.composite.product.ProductCompositeServiceApplicationKt")
	}
}