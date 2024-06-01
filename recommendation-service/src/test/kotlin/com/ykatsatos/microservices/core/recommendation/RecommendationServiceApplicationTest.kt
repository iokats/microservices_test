package com.ykatsatos.microservices.core.recommendation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import com.ykatsatos.api.core.recommendation.Recommendation
import com.ykatsatos.api.event.Event
import com.ykatsatos.api.event.EventType
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.microservices.core.recommendation.persistence.RecommendationRepository
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import java.util.function.Consumer


@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["eureka.client.enabled=false"])
class RecommendationServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: RecommendationRepository

    @Autowired
    @Qualifier("messageProcessor")
    private lateinit var messageProcessor: Consumer<Event<Int, Recommendation>>

    @BeforeEach
    fun setUp() = runBlocking {

        repository.deleteAll()
    }

    @Test
    fun getRecommendationsByProductId(): Unit = runBlocking {

        // given
        val productId = 1

        sendCreateRecommendationEvent(productId, 1)
        sendCreateRecommendationEvent(productId, 2)
        sendCreateRecommendationEvent(productId, 3)

        // when
        val response = getAndVerifyRecommendationsByProductId(productId, OK)

        // then
        assertEquals(3, repository.findByProductId(productId).count())

        response
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[2].productId").isEqualTo(productId)
            .jsonPath("$[2].recommendationId").isEqualTo(3)
    }

    @Test
    fun duplicateError(): Unit = runBlocking {

        // given
        val productId = 2
        val recommendationId = 1

        sendCreateRecommendationEvent(productId, recommendationId)

        assertEquals(1, repository.count())

        // when - then
        val exception = assertThrows(InvalidInputException::class.java, {
            sendCreateRecommendationEvent(productId, recommendationId)
        }, "Expected a InvalidInputException here!")

        assertEquals("Duplicate key, productId: $productId, recommendationId: $recommendationId", exception.message)
    }

    @Test
    fun deleteRecommendations(): Unit = runBlocking {

        // given
        val productId = 1
        val recommendationId = 1

        val recommendation = sendCreateRecommendationEvent(productId, recommendationId)
        assertEquals(1, repository.findByProductId(productId).count())

        // when
        sendDeleteRecommendationEvent(recommendation)

        // then
        assertEquals(0, repository.findByProductId(productId).count())
        sendDeleteRecommendationEvent(recommendation)
    }

    @Test
    fun getRecommendationsMissingParameter(): Unit = runBlocking {

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
    fun getRecommendationsInvalidParameter(): Unit = runBlocking {

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
    fun getRecommendationsNotFound(): Unit = runBlocking {

        // given
        val productIdQuery = "?productionId=113"

        // when
        val response = getAndVerifyRecommendationsByProductId(productIdQuery, OK)

        // then
        response.jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun getRecommendationsInvalidParameterNegativeValue(): Unit = runBlocking {

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

    private fun sendCreateRecommendationEvent(productId: Int, recommendationId: Int): Recommendation {
        val recommendation = Recommendation(
            productId,
            recommendationId,
            "Author $recommendationId",
            recommendationId,
            "Content $recommendationId",
            "SA")

        val event = Event(EventType.CREATE, productId, recommendation)
        messageProcessor.accept(event)
        return recommendation
    }

    private fun sendDeleteRecommendationEvent(recommendation: Recommendation) {
        val event: Event<Int, Recommendation> = Event(EventType.DELETE, recommendation.productId, recommendation)
        messageProcessor.accept(event)
    }
}