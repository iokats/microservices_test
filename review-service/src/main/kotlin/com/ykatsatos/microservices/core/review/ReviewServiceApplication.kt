package com.ykatsatos.microservices.core.review

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
private val LOG = LoggerFactory.getLogger(ReviewServiceApplication::class.java)

@SpringBootApplication
@EnableR2dbcRepositories
@ComponentScan("com.ykatsatos")
class ReviewServiceApplication

fun main(args: Array<String>) {

	val context = runApplication<ReviewServiceApplication>(*args)

	val mysqlUri = context.environment.getProperty("spring.r2dbc.url")

	LOG.info("Connected to MySQL: $mysqlUri")
}
