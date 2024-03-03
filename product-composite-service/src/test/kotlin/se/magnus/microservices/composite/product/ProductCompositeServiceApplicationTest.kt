package se.magnus.microservices.composite.product

import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.MediaType.APPLICATION_JSON
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import se.magnus.api.core.product.Product
import se.magnus.api.core.recommendation.Recommendation
import se.magnus.api.core.review.Review
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.api.exceptions.NotFoundException
import se.magnus.microservices.composite.product.services.ProductCompositeIntegration


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
    fun setUp() {
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
    fun getProductById() {
        client.get()
            .uri("/product-composite/$PRODUCT_ID_OK")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("$.recommendations.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1)
    }

    @Test
    fun getProductNotFound() {
        client.get()
            .uri("/product-composite/$PRODUCT_ID_NOT_FOUND")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/product-composite/$PRODUCT_ID_NOT_FOUND")
            .jsonPath("$.message").isEqualTo("NOT FOUND: $PRODUCT_ID_NOT_FOUND")
    }

    @Test
    fun getProductInvalidInput() {
        client.get()
            .uri("/product-composite/$PRODUCT_ID_INVALID")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/product-composite/$PRODUCT_ID_INVALID")
            .jsonPath("$.message").isEqualTo("INVALID: $PRODUCT_ID_INVALID")
    }
}