package se.magnus.microservices.core.recommendation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("se.magnus")
class RecommendationServiceApplication

fun main(args: Array<String>) {
    runApplication<RecommendationServiceApplication>(*args)
}