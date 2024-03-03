package se.magnus.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import se.magnus.api.core.product.Product
import se.magnus.api.core.product.ProductService
import se.magnus.api.core.recommendation.Recommendation
import se.magnus.api.core.recommendation.RecommendationService
import se.magnus.api.core.review.Review
import se.magnus.api.core.review.ReviewService
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.api.exceptions.NotFoundException
import se.magnus.microservices.utilities.http.HttpErrorInfo
import java.io.IOException


private val LOG: Logger = LoggerFactory.getLogger(ProductCompositeIntegration::class.java)

@Component
class ProductCompositeIntegration @Autowired constructor(
    private val restTemplate: RestTemplate,
    private val mapper: ObjectMapper,
    @param:Value("\${app.product-service.host}") private val productServiceHost: String,
    @param:Value("\${app.product-service.port}") private val productServicePort: String,
    @param:Value("\${app.recommendation-service.host}") private val recommendationServiceHost: String,
    @param:Value("\${app.recommendation-service.port}") private val recommendationServicePort: String,
    @param:Value("\${app.review-service.host}") private val reviewServiceHost: String,
    @param:Value("\${app.review-service.port}") private val reviewServicePort: String,
): ProductService, RecommendationService, ReviewService {

    override fun getProduct(productId: Int): Product {

        try {

            val url = "http://$productServiceHost:$productServicePort/product/$productId"
            LOG.debug("Will call getProduct API on URL: {}", url)

            val product = restTemplate.getForObject(url, Product::class.java)!!
            LOG.debug("Found a product with id: {}", product.productId)

            return product

        } catch (ex: HttpClientErrorException) {

            when(HttpStatus.resolve(ex.statusCode.value())) {
                HttpStatus.NOT_FOUND -> throw NotFoundException(getErrorMessage(ex))
                HttpStatus.UNPROCESSABLE_ENTITY -> throw InvalidInputException(getErrorMessage(ex))
                else -> {
                    LOG.warn("Got an unexpected HTTP error: ${ex.statusCode}, will rethrow it")
                    LOG.warn("Error body: ${ex.responseBodyAsString}")
                    throw ex
                }
            }
        }
    }

    override fun getRecommendations(productId: Int): List<Recommendation> {

        try {

            val url = "http://$recommendationServiceHost:$recommendationServicePort/recommendation?productId=$productId"
            LOG.debug("Will call getRecommendations API on URL: {}", url)

            val recommendations: List<Recommendation> = restTemplate
                .exchange(
                    url,
                    GET,
                    null,
                    object : ParameterizedTypeReference<List<Recommendation>>() {}).body!!

            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size, productId)

            return recommendations
        } catch (ex: Exception) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.message)
            return listOf()
        }
    }

    override fun getReviews(productId: Int): List<Review> {

        try {

            val url = "http://$reviewServiceHost:$reviewServicePort/review?productId=$productId"
            LOG.debug("Will call getReviews API on URL: {}", url)

            val reviews: List<Review> = restTemplate
                .exchange(
                    url,
                    GET,
                    null,
                    object : ParameterizedTypeReference<List<Review>>() {}).body!!

            LOG.debug("Found {} reviews for a product with id: {}", reviews.size, productId)
            return reviews
        } catch (ex: Exception) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.message)
            return listOf()
        }
    }

    private fun getErrorMessage(ex: HttpClientErrorException): String? {
        return try {
            mapper.readValue(ex.responseBodyAsString, HttpErrorInfo::class.java).message
        } catch (_: IOException) {
            ex.message
        }
    }
}