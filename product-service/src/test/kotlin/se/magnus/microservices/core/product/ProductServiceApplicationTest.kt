package se.magnus.microservices.core.product

import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient


@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun getProductById() {
        val productId = 1

        client.get()
            .uri("/product/$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.productId").isEqualTo(productId)
    }

    @Test
    fun getProductInvalidParameterString() {

        client.get()
            .uri("/product/no-integer")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(BAD_REQUEST)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/product/no-integer")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getProductNotFound() {
        val productIdNotFound = 13

        client.get()
            .uri("/product/$productIdNotFound")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/product/$productIdNotFound")
            .jsonPath("$.message").isEqualTo("No product found for productId: $productIdNotFound")
    }

    @Test
    fun getProductInvalidParameterNegativeValue() {
        val productIdInvalid = -1

        client.get()
            .uri("/product/$productIdInvalid")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/product/$productIdInvalid")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }
}