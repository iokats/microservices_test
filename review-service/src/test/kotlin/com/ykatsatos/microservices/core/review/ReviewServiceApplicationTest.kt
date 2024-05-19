package com.ykatsatos.microservices.core.review
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
import com.ykatsatos.api.core.review.Review
import com.ykatsatos.api.event.Event
import com.ykatsatos.api.event.EventType
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.microservices.core.review.persistence.ReviewRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Qualifier
import java.util.function.Consumer


@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ReviewRepository

    @Autowired
    @Qualifier("messageProcessor")
    private lateinit var messageProcessor: Consumer<Event<Int, Review>>

    @BeforeEach
    fun setUp() = runBlocking {

        repository.deleteAll()
    }

    @Test
    fun getReviewsByProductId(): Unit = runBlocking {

        // given
        val productId = 1

        assertEquals(0, repository.findByProductId(productId).count())

        sendCreateReviewEvent(productId, 1)
        sendCreateReviewEvent(productId, 2)
        sendCreateReviewEvent(productId, 3)

        assertEquals(3, repository.findByProductId(productId).count())

        // when
        val response = getAndVerifyReviewsByProductId(productId, OK)

        // then
        assertEquals(3, repository.findByProductId(productId))

        response
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[2].productId").isEqualTo(productId)
            .jsonPath("$[2].reviewId").isEqualTo(3)
    }

    @Test
    fun duplicateError(): Unit = runBlocking {

        // given
        val productId = 2
        val reviewId = 2

        assertEquals(0, repository.count())

        sendCreateReviewEvent(productId, reviewId)

        assertEquals(1, repository.count())

        // when - then
        val exception = Assertions.assertThrows(InvalidInputException::class.java, {
            sendCreateReviewEvent(productId, reviewId)
        }, "Expected a InvalidInputException here!")

        assertEquals("Duplicate key, productId: $productId, reviewId: $reviewId", exception.message)
    }

    @Test
    fun deleteReviews(): Unit = runBlocking {

        // given
        val productId = 1
        val reviewId = 1

        val review = sendCreateReviewEvent(productId, reviewId)
        assertEquals(1, repository.findByProductId(productId).count())

        // when
        sendDeleteReviewEvent(review)

        // then
        assertEquals(0, repository.findByProductId(productId).count())
        sendDeleteReviewEvent(review)
    }

    @Test
    fun getReviewsMissingParameter(): Unit = runBlocking {

        // given
        val productIdQuery = ""

        // when
        val response = getAndVerifyReviewsByProductId(productIdQuery, BAD_REQUEST)

        // then
        response
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.")
    }

    @Test
    fun getReviewsInvalidParameter(): Unit = runBlocking {

        // given
        val productIdQuery = "?productId=no-integer"

        // when
        val response = getAndVerifyReviewsByProductId(productIdQuery, UNPROCESSABLE_ENTITY)

        // then
        response
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getReviewsNotFound(): Unit = runBlocking {

        // given
        val productIdNotFound = "?productId=213"

        // when
        val response = getAndVerifyReviewsByProductId(productIdNotFound, OK)

        // then
        response.jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun getReviewsInvalidParameterNegativeValue(): Unit = runBlocking {

        // given
        val productIdInvalid = -1

        // when
        val response = getAndVerifyReviewsByProductId("?productId=$productIdInvalid", UNPROCESSABLE_ENTITY)

        // then
        response
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

    private fun getAndVerifyReviewsByProductId(productId: Int, expectedStatus: HttpStatus): WebTestClient.BodyContentSpec {

        return getAndVerifyReviewsByProductId("?productId=$productId", expectedStatus)
    }

    private fun getAndVerifyReviewsByProductId(productIdQuery: String, expectedStatus: HttpStatus): WebTestClient.BodyContentSpec {

        return client.get()
            .uri("/review$productIdQuery")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun sendCreateReviewEvent(productId: Int, reviewId: Int): Review {
        val review = Review(
            productId,
            reviewId,
            "Author $reviewId",
            "Subject $reviewId",
            "Content $reviewId",
            "SA")

        val event = Event(EventType.CREATE, productId, review)
        messageProcessor.accept(event)
        return review
    }

    private fun sendDeleteReviewEvent(review: Review) {
        val event: Event<Int, Review> = Event(EventType.DELETE, review.productId, review)
        messageProcessor.accept(event)
    }
}