package com.ykatsatos.microservices.composite.product

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Mono.just
import com.ykatsatos.api.composite.product.ProductAggregate
import com.ykatsatos.api.composite.product.RecommendationSummary
import com.ykatsatos.api.composite.product.ReviewSummary
import com.ykatsatos.api.core.product.Product
import com.ykatsatos.api.core.recommendation.Recommendation
import com.ykatsatos.api.core.review.Review
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.api.exceptions.NotFoundException
import com.ykatsatos.microservices.composite.product.services.ProductCompositeIntegration
import kotlinx.coroutines.runBlocking


private const val PRODUCT_ID_OK = 1
private const val PRODUCT_ID_NOT_FOUND = 2
private const val PRODUCT_ID_INVALID = 3

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @MockBean
    private lateinit var compositeIntegration: ProductCompositeIntegration

    @BeforeEach
    fun setUp(): Unit = runBlocking {

        `when`(compositeIntegration.getProduct(PRODUCT_ID_OK))
            .thenReturn(Product(PRODUCT_ID_OK, "name", 1, "mock-address"))
        `when`(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
            .thenReturn(listOf(Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")))
        `when`(compositeIntegration.getReviews(PRODUCT_ID_OK))
            .thenReturn(listOf(Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")))

        `when`(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
            .thenThrow(NotFoundException("NOT FOUND: $PRODUCT_ID_NOT_FOUND"))

        `when`(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
            .thenThrow(InvalidInputException("INVALID: $PRODUCT_ID_INVALID"))
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun createCompositeProduct1() {

        val compositeProduct = ProductAggregate(1, "name", 123, listOf(), listOf())

        postAndVerifyProduct(compositeProduct, OK)
    }

    @Test
    fun createCompositeProduct2() {

        val recommendations = listOf(RecommendationSummary(1, "a", 1, "c"))
        val reviews = listOf(ReviewSummary(1, "a", "s", "c"))
        val compositeProduct = ProductAggregate(
            1,
            "name",
            123,
            recommendations,
            reviews)

        postAndVerifyProduct(compositeProduct, OK)
    }

    @Test
    fun deleteCompositeProduct() {
        val recommendations = listOf(RecommendationSummary(1, "a", 1, "c"))
        val reviews = listOf(ReviewSummary(1, "a", "s", "c"))
        val compositeProduct = ProductAggregate(1,
            "name",
            1,
            recommendations,
            reviews)

        postAndVerifyProduct(compositeProduct, OK)

        deleteAndVerifyProduct(compositeProduct.productId, OK)
        deleteAndVerifyProduct(compositeProduct.productId, OK)
    }

    @Test
    fun getProductById() {

        getAndVerifyProduct(PRODUCT_ID_OK, OK)
            .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("$.recommendations.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1)
    }

    @Test
    fun getProductNotFound() {

        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product-composite/$PRODUCT_ID_NOT_FOUND")
            .jsonPath("$.message").isEqualTo("NOT FOUND: $PRODUCT_ID_NOT_FOUND")
    }

    @Test
    fun getProductInvalidInput() {

        getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product-composite/$PRODUCT_ID_INVALID")
            .jsonPath("$.message").isEqualTo("INVALID: $PRODUCT_ID_INVALID")
    }

    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {

        return client.get()
            .uri("/product-composite/$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyProduct(compositeProduct: ProductAggregate, expectedStatus: HttpStatus) {

        client.post()
            .uri("/product-composite")
            .body(just(compositeProduct), ProductAggregate::class.java)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }

    private fun deleteAndVerifyProduct(productId: Int, expectedStatus: HttpStatus) {

        client.delete()
            .uri("/product-composite/$productId")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }
}