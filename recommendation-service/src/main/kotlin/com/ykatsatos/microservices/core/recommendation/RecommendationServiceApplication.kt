package com.ykatsatos.microservices.core.recommendation

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

private val LOG = LoggerFactory.getLogger(RecommendationServiceApplication::class.java)

@SpringBootApplication
@ComponentScan("com.ykatsatos")
class RecommendationServiceApplication

fun main(args: Array<String>) {

    val context = runApplication<RecommendationServiceApplication>(*args)

    val mongoDbHost = context.environment.getProperty("spring.data.mongodb.host")
    val mongoDbPort = context.environment.getProperty("spring.data.mongodb.port")

    LOG.info("Connected to MongoDb: $mongoDbHost:$mongoDbPort")
}