/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.spring.plugin)
    implementation(libs.kotlin.kapt.plugin)
    implementation(libs.kotlin.jpa.plugin)
    implementation(libs.spring.boot.plugin)
    implementation(libs.spring.data.mongodb)
    implementation(libs.spring.data.jpa)
    implementation(libs.mysql.connector.j)
    implementation(libs.dependency.management.plugin)
    implementation(libs.mapstruct)
    implementation(libs.mapstruct.spring.extension)
    implementation(libs.mapstruct.processor)
    implementation(libs.testcontainers.bom)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.mysql)
}