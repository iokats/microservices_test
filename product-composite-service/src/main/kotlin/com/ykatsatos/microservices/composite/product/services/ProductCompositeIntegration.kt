package com.ykatsatos.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.ykatsatos.api.core.product.Product
import com.ykatsatos.api.core.product.ProductService
import com.ykatsatos.api.core.recommendation.Recommendation
import com.ykatsatos.api.core.recommendation.RecommendationService
import com.ykatsatos.api.core.review.Review
import com.ykatsatos.api.core.review.ReviewService
import com.ykatsatos.api.event.Event
import com.ykatsatos.api.event.EventType
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.api.exceptions.NotFoundException
import com.ykatsatos.microservices.utilities.http.HttpErrorInfo
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.io.IOException
import java.util.logging.Level


private val LOG = LoggerFactory.getLogger(ProductCompositeIntegration::class.java)

@Component
class ProductCompositeIntegration @Autowired constructor(
    webClientBuilder: WebClient.Builder,
    private val mapper: ObjectMapper,
    private val streamBridge: StreamBridge,
): ProductService, RecommendationService, ReviewService {

    private val productServiceUrl = "http://product"
    private val recommendationServiceUrl = "http://recommendation"
    private val reviewServiceUrl = "http://review"
    private val webClient = webClientBuilder.build()

    override suspend fun createProduct(body: Product): Product {

        sendMessage("products-out-0", Event(EventType.CREATE, body.productId, body))

        return body
    }

    override suspend fun getProduct(productId: Int): Product {

        val url = "$productServiceUrl/product/$productId"
        LOG.debug("Will call getProduct API on URL: {}", url)

        return webClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<Product>()
            .onErrorMap(WebClientResponseException::class.java) {ex -> handleException(ex)}
            .awaitFirst()
    }

    override suspend fun deleteProduct(productId: Int) {

        sendMessage("products-out-0", Event<Int, Product>(EventType.DELETE, productId))
    }

    override suspend fun createRecommendation(body: Recommendation): Recommendation {

        sendMessage("recommendations-out-0", Event(EventType.CREATE, body.productId, body))

        return body
    }

    override suspend fun getRecommendations(productId: Int): List<Recommendation> {

        val url = "$recommendationServiceUrl/recommendation?productId=$productId"
        LOG.debug("Will call getRecommendations API on URL: {}", url)

        return webClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<Recommendation>>()
            .log(LOG.name, Level.FINE)
            .onErrorResume { _ -> Mono.just(emptyList()) }
            .awaitFirst()
    }

    override suspend fun deleteRecommendations(productId: Int) {

        sendMessage("recommendations-out-0", Event<Int, Product>(EventType.DELETE, productId))
    }

    override suspend fun createReview(body: Review): Review {

        sendMessage("reviews-out-0", Event(EventType.CREATE, body.productId, body))

        return body
    }

    override suspend fun getReviews(productId: Int): List<Review> {

        val url = "$reviewServiceUrl/review?productId=$productId"
        LOG.debug("Will call getReviews API on URL: {}", url)

        return webClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<Review>>()
            .log(LOG.name, Level.FINE)
            .onErrorResume { _ -> Mono.just(emptyList()) }
            .awaitFirst()
    }

    override suspend fun deleteReviews(productId: Int) {

        sendMessage("reviews-out-0", Event<Int, Product>(EventType.DELETE, productId))
    }

    fun getProductHealth(): Mono<Health> {
        return getHealth(productServiceUrl)
    }

    fun getRecommendationHealth(): Mono<Health> {
        return getHealth(recommendationServiceUrl)
    }

    fun getReviewHealth(): Mono<Health> {
        return getHealth(reviewServiceUrl)
    }

    private fun <T> sendMessage(bindingName: String, event: Event<Int, T>) {
        LOG.debug("Sending a {} message to {}", event.eventType, bindingName)

        val message = MessageBuilder
            .withPayload(event)
            .setHeader("partitionKey", event.key)
            .build()

        streamBridge.send(bindingName, message)
    }

    private fun getHealth(url: String): Mono<Health> {

        val healthUrl = "$url/actuator/health"
        LOG.debug("Will call the Health API on URL: {}", healthUrl)

        return webClient.get()
            .uri(healthUrl)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { Health.Builder().up().build()  }
            .onErrorResume { ex -> Mono.just(Health.Builder().down(ex).build()) }
            .log(LOG.name, Level.FINE)
    }

    private fun handleException(ex: WebClientResponseException): Throwable {

        return when (HttpStatus.resolve(ex.statusCode.value())) {

            HttpStatus.NOT_FOUND -> NotFoundException(getErrorMessage(ex))

            HttpStatus.UNPROCESSABLE_ENTITY -> InvalidInputException(getErrorMessage(ex))

            else -> {
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.statusCode)
                LOG.warn("Error body: {}", ex.responseBodyAsString)
                ex
            }
        }
    }

    private fun getErrorMessage(ex: WebClientResponseException): String? {
        return try {
            mapper.readValue(ex.responseBodyAsString, HttpErrorInfo::class.java).message
        } catch (ex: IOException) {
            ex.message
        }
    }
}