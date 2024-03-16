package se.magnus.microservices.core.product

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

private val LOG = LoggerFactory.getLogger(ProductServiceApplication::class.java)

@SpringBootApplication
@ComponentScan("se.magnus")
class ProductServiceApplication

fun main(args: Array<String>) {

    val context = runApplication<ProductServiceApplication>(*args)

    val mongoDbHost = context.environment.getProperty("spring.data.mongodb.host")
    val mongoDbPort = context.environment.getProperty("spring.data.mongodb.port")

    LOG.info("Connected to MongoDb: $mongoDbHost:$mongoDbPort")
}