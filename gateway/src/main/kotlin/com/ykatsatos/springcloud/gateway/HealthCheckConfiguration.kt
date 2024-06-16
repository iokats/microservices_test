package com.ykatsatos.springcloud.gateway

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthContributor
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.logging.Level

private val LOG = LoggerFactory.getLogger(HealthCheckConfiguration::class.java)

@Configuration
class HealthCheckConfiguration @Autowired constructor(webClientBuilder: WebClient.Builder) {
    private val productServiceUrl = "http://product"
    private val recommendationServiceUrl = "http://recommendation"
    private val reviewServiceUrl = "http://review"
    private val productCompositeServiceUrl = "http://product-composite"
    private val webClient = webClientBuilder.build()

    @Bean
    fun healthcheckMicroservices(): ReactiveHealthContributor {

        val registry: MutableMap<String, ReactiveHealthIndicator> = LinkedHashMap()

        registry["product"] = ReactiveHealthIndicator { getHealth(productServiceUrl) }
        registry["recommendation"] = ReactiveHealthIndicator { getHealth(recommendationServiceUrl) }
        registry["review"] = ReactiveHealthIndicator { getHealth(reviewServiceUrl) }
        registry["product-composite"] = ReactiveHealthIndicator { getHealth(productCompositeServiceUrl) }

        return CompositeReactiveHealthContributor.fromMap(registry)
    }

    private fun getHealth(baseUrl: String): Mono<Health> {
        val url = "$baseUrl/actuator/health"
        LOG.debug("Setting up call to the Health API on URL: {}", url)

        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { Health.Builder().up().build()  }
            .onErrorResume { ex -> Mono.just(Health.Builder().down(ex).build()) }
            .log(LOG.name, Level.FINE)
    }
}