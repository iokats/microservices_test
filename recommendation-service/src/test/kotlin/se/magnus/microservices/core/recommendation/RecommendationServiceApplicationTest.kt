package se.magnus.microservices.core.recommendation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import se.magnus.api.core.recommendation.Recommendation
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository


@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: RecommendationRepository

    @BeforeEach
    fun setUp() {

        repository.deleteAll()
    }

    @Test
    fun getRecommendationsByProductId() {

        // given
        val productId = 1

        postAndVerifyRecommendation(productId, 1, OK)
        postAndVerifyRecommendation(productId, 2, OK)
        postAndVerifyRecommendation(productId, 3, OK)

        // when
        val response = getAndVerifyRecommendationsByProductId(productId, OK)

        // then
        assertEquals(3, repository.findByProductId(productId).size)

        response
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[2].productId").isEqualTo(productId)
            .jsonPath("$[2].recommendationId").isEqualTo(3)
    }

    @Test
    fun duplicateError() {

        // given
        val productId = 1
        val recommendationId = 1

        // when
        postAndVerifyRecommendation(productId, recommendationId, OK)
            .jsonPath("$.productId").isEqualTo(productId)
            .jsonPath("$.recommendationId").isEqualTo(recommendationId)

        assertEquals(1, repository.count())

        // then
        postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Duplicate key, productId: $productId, recommendationId: $recommendationId")
    }

    @Test
    fun deleteRecommendations() {

        // given
        val productId = 1
        val recommendationId = 1

        postAndVerifyRecommendation(productId, recommendationId, OK)
        assertEquals(1, repository.findByProductId(productId).size)

        // when
        deleteAndVerifyRecommendationsByProductId(productId, OK)

        // then
        assertEquals(0, repository.findByProductId(productId).size)
        deleteAndVerifyRecommendationsByProductId(productId, OK)
    }

    @Test
    fun getRecommendationsMissingParameter() {

        // given
        val productIdQuery = ""

        // when
        val response = getAndVerifyRecommendationsByProductId(productIdQuery, BAD_REQUEST)

        // then
        response
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.")
    }

    @Test
    fun getRecommendationsInvalidParameter() {

        // given
        val productIdQuery = "?productId=no-integer"

        // when
        val response = getAndVerifyRecommendationsByProductId(productIdQuery, UNPROCESSABLE_ENTITY)

        // then
        response
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getRecommendationsNotFound() {

        // given
        val productIdQuery = "?productionId=113"

        // when
        val response = getAndVerifyRecommendationsByProductId(productIdQuery, OK)

        // then
        response.jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun getRecommendationsInvalidParameterNegativeValue() {

        // given
        val productIdInvalid = -1

        // when
        val response = getAndVerifyRecommendationsByProductId("?productId=$productIdInvalid", UNPROCESSABLE_ENTITY)

        // then
        response
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

    private fun getAndVerifyRecommendationsByProductId(productId: Int, expectedStatus: HttpStatus): WebTestClient.BodyContentSpec {

        return getAndVerifyRecommendationsByProductId("?productId=$productId", expectedStatus)
    }

    private fun getAndVerifyRecommendationsByProductId(productIdQuery: String, expectedStatus: HttpStatus): WebTestClient.BodyContentSpec {

        return client.get()
            .uri("/recommendation$productIdQuery")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyRecommendation(
        productId: Int,
        recommendationId: Int,
        expectedStatus: HttpStatus
    ): WebTestClient.BodyContentSpec {

        val newRecommendation = Recommendation(
            productId,
            recommendationId,
            "Author $recommendationId",
            recommendationId,
            "Content $recommendationId",
            "SA"
        )

        return client.post()
            .uri("/recommendation").
            body(Mono.just(newRecommendation), Recommendation::class.java)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun deleteAndVerifyRecommendationsByProductId(
        productId: Int,
        expectedStatus: HttpStatus
    ): WebTestClient.BodyContentSpec {

        return client.delete()
            .uri("/recommendation?productId=$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }
}