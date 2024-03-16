package se.magnus.microservices.core.review

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

private val LOG = LoggerFactory.getLogger(ReviewServiceApplication::class.java)

@SpringBootApplication
@ComponentScan("se.magnus")
class ReviewServiceApplication

fun main(args: Array<String>) {

	val context = runApplication<ReviewServiceApplication>(*args)

	val mysqlUri = context.environment.getProperty("spring.datasource.url")

	LOG.info("Connected to MySQL: $mysqlUri")
}
