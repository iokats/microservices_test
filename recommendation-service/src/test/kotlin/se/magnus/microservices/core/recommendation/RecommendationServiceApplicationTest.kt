package se.magnus.microservices.core.recommendation

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient


@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun getRecommendationsByProductId() {

        val productId = 1

        client.get()
            .uri("/recommendation?productId=$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].productId").isEqualTo(productId)
    }

    @Test
    fun getRecommendationsMissingParameter() {

        client.get()
            .uri("/recommendation")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(BAD_REQUEST)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.")
    }


    @Test
    fun getRecommendationsInvalidParameter() {

        client.get()
            .uri("/recommendation?productId=no-integer")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(BAD_REQUEST)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getRecommendationsNotFound() {

        val productIdNotFound = 113

        client.get()
            .uri("/recommendation?productId=$productIdNotFound")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun getRecommendationsInvalidParameterNegativeValue() {

        val productIdInvalid = -1

        client.get()
            .uri("/recommendation?productId=$productIdInvalid")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }
}