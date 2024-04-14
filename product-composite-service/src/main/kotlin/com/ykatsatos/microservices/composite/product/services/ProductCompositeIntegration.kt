package com.ykatsatos.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import com.ykatsatos.api.core.product.Product
import com.ykatsatos.api.core.product.ProductService
import com.ykatsatos.api.core.recommendation.Recommendation
import com.ykatsatos.api.core.recommendation.RecommendationService
import com.ykatsatos.api.core.review.Review
import com.ykatsatos.api.core.review.ReviewService
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.api.exceptions.NotFoundException
import com.ykatsatos.microservices.utilities.http.HttpErrorInfo
import java.io.IOException


private val LOG = LoggerFactory.getLogger(ProductCompositeIntegration::class.java)

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

    private val productServiceUrl = "http://$productServiceHost:$productServicePort/product"
    private val recommendationServiceUrl = "http://$recommendationServiceHost:$recommendationServicePort/recommendation"
    private val reviewServiceUrl = "http://$reviewServiceHost:$reviewServicePort/review"

    override fun createProduct(body: Product): Product {

        try {
            val url = productServiceUrl
            LOG.debug("Will post a new product to URL: $url")

            val product = restTemplate.postForObject(url, body, Product::class.java)!!
            LOG.debug("Created a product with id ${product.productId}")

            return product

        } catch (ex: HttpClientErrorException) {

            throw handleHttpClientException(ex)
        }
    }

    override fun getProduct(productId: Int): Product {

        try {

            val url = "$productServiceUrl/$productId"

            LOG.debug("Will call getProduct API on URL: {}", url)

            val product = restTemplate.getForObject(url, Product::class.java)!!
            LOG.debug("Found a product with id: {}", product.productId)

            return product

        } catch (ex: HttpClientErrorException) {

            throw handleHttpClientException(ex)
        }
    }

    override fun deleteProduct(productId: Int) {

        try {

            val url = "$productServiceUrl/$productId"
            LOG.debug("Will call the deleteProduct API on URL: $url")

            restTemplate.delete(url)

        } catch (ex: HttpClientErrorException) {

            throw handleHttpClientException(ex)
        }
    }

    override fun createRecommendation(body: Recommendation): Recommendation {

        try {
            val url = recommendationServiceUrl
            LOG.debug("Will post a new recommendation to URL: $url")

            val recommendation = restTemplate.postForObject(url, body, Recommendation::class.java)!!
            LOG.debug("Created a recommendation with id ${recommendation.productId}")

            return recommendation

        } catch (ex: HttpClientErrorException) {

            throw handleHttpClientException(ex)
        }
    }

    override fun getRecommendations(productId: Int): List<Recommendation> {

        try {

            val url = "$recommendationServiceUrl?productId=$productId"
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

    override fun deleteRecommendations(productId: Int) {

        try {

            val url = "$recommendationServiceUrl?productId=$productId"
            LOG.debug("Will call the deleteRecommendation API on URL: $url")

            restTemplate.delete(url)

        } catch (ex: HttpClientErrorException) {

            throw handleHttpClientException(ex)
        }
    }

    override fun createReview(body: Review): Review {

        try {
            val url = reviewServiceUrl
            LOG.debug("Will post a new review to URL: $url")

            val review = restTemplate.postForObject(url, body, Review::class.java)!!
            LOG.debug("Created a review with id ${review.productId}")

            return review

        } catch (ex: HttpClientErrorException) {

            throw handleHttpClientException(ex)
        }
    }

    override fun getReviews(productId: Int): List<Review> {

        try {

            val url = "$reviewServiceUrl?productId=$productId"
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

    override suspend fun deleteReviews(productId: Int) {

        try {

            val url = "$reviewServiceUrl?productId=$productId"
            LOG.debug("Will call the deleteReviews API on URL: $url")

            restTemplate.delete(url)

        } catch (ex: HttpClientErrorException) {

            throw handleHttpClientException(ex)
        }
    }

    private fun handleHttpClientException(ex: HttpClientErrorException): RuntimeException {
        when (HttpStatus.resolve(ex.statusCode.value())) {
            HttpStatus.NOT_FOUND -> return NotFoundException(getErrorMessage(ex))

            HttpStatus.UNPROCESSABLE_ENTITY -> return InvalidInputException(getErrorMessage(ex))

            else -> {
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.statusCode)
                LOG.warn("Error body: {}", ex.responseBodyAsString)
                return ex
            }
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