package se.magnus.microservices.core.review
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient


@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun getReviewsByProductId() {

        val productId = 1

        client.get()
            .uri("/review?productId=$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].productId").isEqualTo(productId)
    }

    @Test
    fun getReviewsMissingParameter() {

        client.get()
            .uri("/review")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(BAD_REQUEST)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.")
    }

    @Test
    fun getReviewsInvalidParameter() {

        client.get()
            .uri("/review?productId=no-integer")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(BAD_REQUEST)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getReviewsNotFound() {

        val productIdNotFound = 213

        client.get()
            .uri("/review?productId=$productIdNotFound")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun getReviewsInvalidParameterNegativeValue() {

        val productIdInvalid = -1

        client.get()
            .uri("/review?productId=$productIdInvalid")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }
}