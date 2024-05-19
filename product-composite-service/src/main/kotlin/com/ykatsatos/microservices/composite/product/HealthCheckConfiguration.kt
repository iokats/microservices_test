package com.ykatsatos.microservices.composite.product

import com.ykatsatos.microservices.composite.product.services.ProductCompositeIntegration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor
import org.springframework.boot.actuate.health.ReactiveHealthContributor
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HealthCheckConfiguration @Autowired constructor(private val integration: ProductCompositeIntegration) {

    @Bean
    fun coreServices(): ReactiveHealthContributor {

        val registry: MutableMap<String, ReactiveHealthIndicator> = LinkedHashMap()

        registry["product"] = ReactiveHealthIndicator { integration.getProductHealth() }
        registry["recommendation"] = ReactiveHealthIndicator { integration.getRecommendationHealth() }
        registry["review"] = ReactiveHealthIndicator { integration.getReviewHealth() }

        return CompositeReactiveHealthContributor.fromMap(registry)
    }
}