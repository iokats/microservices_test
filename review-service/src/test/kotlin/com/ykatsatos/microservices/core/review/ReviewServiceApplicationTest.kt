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
import reactor.core.publisher.Mono
import com.ykatsatos.api.core.review.Review
import com.ykatsatos.microservices.core.review.persistence.ReviewRepository
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking


@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ReviewRepository

    @BeforeEach
    fun setUp() = runBlocking {

        repository.deleteAll()
    }

    @Test
    fun getReviewsByProductId(): Unit = runBlocking {

        // given
        val productId = 1

        assertEquals(0, repository.findByProductId(productId).count())

        postAndVerifyReview(productId, 1, OK)
        postAndVerifyReview(productId, 2, OK)
        postAndVerifyReview(productId, 3, OK)

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

        postAndVerifyReview(productId, reviewId, OK)
            .jsonPath("$.productId").isEqualTo(productId)
            .jsonPath("$.reviewId").isEqualTo(reviewId)

        assertEquals(1, repository.count())

        // when
        val response = postAndVerifyReview(productId, reviewId, UNPROCESSABLE_ENTITY)

        // then
        response
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Duplicate key, productId: $productId, reviewId: $reviewId")
    }

    @Test
    fun deleteReviews(): Unit = runBlocking {

        // given
        val productId = 1
        val reviewId = 1

        postAndVerifyReview(productId, reviewId, OK)
        assertEquals(1, repository.findByProductId(productId).count())

        // when
        deleteAndVerifyReviewsByProductId(productId, OK)

        // then
        assertEquals(0, repository.findByProductId(productId).count())
        deleteAndVerifyReviewsByProductId(productId, OK)
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

    private fun postAndVerifyReview(
        productId: Int,
        reviewId: Int,
        expectedStatus: HttpStatus
    ): WebTestClient.BodyContentSpec {

        val newReview = Review(
            productId,
            reviewId,
            "Author $reviewId",
            "Subject $reviewId",
            "Content $reviewId",
            "SA"
        )

        return client.post()
            .uri("/review").
            body(Mono.just(newReview), Review::class.java)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun deleteAndVerifyReviewsByProductId(
        productId: Int,
        expectedStatus: HttpStatus
    ): WebTestClient.BodyContentSpec {

        return client.delete()
            .uri("/review?productId=$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }
}