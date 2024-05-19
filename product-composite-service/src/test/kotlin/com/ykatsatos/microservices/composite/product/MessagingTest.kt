package com.ykatsatos.microservices.composite.product

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.ykatsatos.api.composite.product.ProductAggregate
import com.ykatsatos.api.composite.product.RecommendationSummary
import com.ykatsatos.api.composite.product.ReviewSummary
import com.ykatsatos.api.core.product.Product
import com.ykatsatos.api.core.recommendation.Recommendation
import com.ykatsatos.api.core.review.Review
import com.ykatsatos.api.event.Event
import com.ykatsatos.api.event.EventType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.io.IOException


private val LOG = LoggerFactory.getLogger(MessagingTest::class.java)

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["spring.main.allow-bean-definition-overriding=true"])
@Import(TestChannelBinderConfiguration::class)
class MessagingTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var target: OutputDestination

    private val mapper = ObjectMapper().registerModule(JavaTimeModule())

    @BeforeEach
    fun setUp() = runBlocking {
        purgeMessages("products")
        purgeMessages("recommendations")
        purgeMessages("reviews")
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun createCompositeProduct1() {

        val compositeProduct = ProductAggregate(1, "name", 123, listOf(), listOf())

        postAndVerifyProduct(compositeProduct, HttpStatus.OK)

        val productMessages = getMessages("products")
        val recommendationMessages = getMessages("recommendations")
        val reviewMessages = getMessages("reviews")

        // Assert one expected new product event queued up
        assertEquals(1, productMessages.size)

        val expectedEvent: Event<Int, Product> = Event(
            EventType.CREATE,
            compositeProduct.productId,
            Product(compositeProduct.productId, compositeProduct.name, compositeProduct.weight, null)
        )
        isSameProductEvent(expectedEvent, productMessages[0])

        // Assert no recommendation and review events
        assertEquals(0, recommendationMessages.size)
        assertEquals(0, reviewMessages.size)
    }

    @Test
    fun createCompositeProduct2() {
        val composite = ProductAggregate(
            1, "name", 1,
            listOf(RecommendationSummary(1, "a", 1, "c")),
            listOf(ReviewSummary(1, "a", "s", "c")), null
        )
        postAndVerifyProduct(composite, HttpStatus.OK)

        val productMessages = getMessages("products")
        val recommendationMessages = getMessages("recommendations")
        val reviewMessages = getMessages("reviews")

        // Assert one create product event queued up
        assertEquals(1, productMessages.size)

        val expectedProductEvent = Event(
            EventType.CREATE,
            composite.productId,
            Product(composite.productId, composite.name, composite.weight, null)
        )
        isSameProductEvent(expectedProductEvent, productMessages[0])

        // Assert one create recommendation event queued up
        assertEquals(1, recommendationMessages.size)

        val rec = composite.recommendations[0]
        val expectedRecommendationEvent: Event<Int, Recommendation> = Event(
            EventType.CREATE,
            composite.productId,
            Recommendation(composite.productId, rec.recommendationId, rec.author, rec.rate, rec.content, null)
        )
        isSameRecommendationEvent(expectedRecommendationEvent, recommendationMessages[0])

        // Assert one create review event queued up
        assertEquals(1, reviewMessages.size)

        val rev = composite.reviews[0]
        val expectedReviewEvent: Event<Int, Review> = Event(
            EventType.CREATE,
            composite.productId,
            Review(composite.productId, rev.reviewId, rev.author, rev.subject, rev.content, null)
        )
        isSameReviewEvent(expectedReviewEvent, reviewMessages[0])
    }

    @Test
    fun deleteCompositeProduct() {
        deleteAndVerifyProduct(1, HttpStatus.OK)

        val productMessages = getMessages("products")
        val recommendationMessages = getMessages("recommendations")
        val reviewMessages = getMessages("reviews")

        // Assert one delete product event queued up
        assertEquals(1, productMessages.size)

        val expectedProductEvent: Event<Int, Product> = Event(EventType.DELETE, 1, null)
        isSameProductEvent(expectedProductEvent, productMessages[0])

        // Assert one delete recommendation event queued up
        assertEquals(1, recommendationMessages.size)

        val expectedRecommendationEvent: Event<Int, Recommendation> = Event(EventType.DELETE, 1)
        isSameRecommendationEvent(expectedRecommendationEvent, recommendationMessages[0])

        // Assert one delete review event queued up
        assertEquals(1, reviewMessages.size)

        val expectedReviewEvent: Event<Int, Review> = Event(EventType.DELETE, 1)
        isSameReviewEvent(expectedReviewEvent, reviewMessages[0])
    }

    private fun isSameProductEvent(expectedEvent: Event<Int, Product>, eventAsJson: String) {
        val event = convertJsonStringToMap(eventAsJson)
        val eventData = event["data"] as? Map<*, *>

        assertEquals(expectedEvent.eventType.toString(), event["eventType"])
        assertEquals(expectedEvent.key, event["key"])
        eventData?.let { assertEquals(expectedEvent.data?.productId, it["productId"]) }
        eventData?.let {assertEquals(expectedEvent.data?.name, it["name"]) }
        eventData?.let {assertEquals(expectedEvent.data?.weight, it["weight"]) }
    }

    private fun isSameRecommendationEvent(expectedEvent: Event<Int, Recommendation>, eventAsJson: String) {
        val event = convertJsonStringToMap(eventAsJson)
        val eventData = event["data"] as? Map<*, *>

        assertEquals(expectedEvent.eventType.toString(), event["eventType"])
        assertEquals(expectedEvent.key, event["key"])
        eventData?.let { assertEquals(expectedEvent.data?.productId, eventData["productId"]) }
        eventData?.let { assertEquals(expectedEvent.data?.recommendationId, eventData["recommendationId"]) }
        eventData?.let { assertEquals(expectedEvent.data?.author, eventData["author"]) }
        eventData?.let { assertEquals(expectedEvent.data?.rate, eventData["rate"]) }
        eventData?.let { assertEquals(expectedEvent.data?.content, eventData["content"]) }
    }

    private fun isSameReviewEvent(expectedEvent: Event<Int, Review>, eventAsJson: String) {
        val event = convertJsonStringToMap(eventAsJson)
        val eventData = event["data"] as? Map<*, *>

        assertEquals(expectedEvent.eventType.toString(), event["eventType"])
        assertEquals(expectedEvent.key, event["key"])
        eventData?.let { assertEquals(expectedEvent.data?.productId, eventData["productId"]) }
        eventData?.let { assertEquals(expectedEvent.data?.reviewId, eventData["reviewId"]) }
        eventData?.let { assertEquals(expectedEvent.data?.author, eventData["author"]) }
        eventData?.let { assertEquals(expectedEvent.data?.subject, eventData["subject"]) }
        eventData?.let { assertEquals(expectedEvent.data?.content, eventData["content"]) }
    }

    private fun convertJsonStringToMap(eventAsJson: String): MutableMap<*, *> {
        try {
            return mapper.readValue(eventAsJson, object : TypeReference<HashMap<*, *>>() {})
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun purgeMessages(bindingName: String) {
        getMessages(bindingName)
    }

    private fun getMessages(bindingName: String): List<String> {
        val messages: MutableList<String> = ArrayList()
        var anyMoreMessages = true

        while (anyMoreMessages) {
            val message = getMessage(bindingName)

            if (message == null) {
                anyMoreMessages = false
            } else {
                messages.add(String(message.payload))
            }
        }

        return messages
    }

    private fun getMessage(bindingName: String): Message<ByteArray>? {
        try {
            return target.receive(0, bindingName)
        } catch (npe: NullPointerException) {
            // If the messageQueues member variable in the target object contains no queues when the receive method is
            // called, it will cause a NPE to be thrown. So we catch the NPE here and return null to indicate that no
            // messages were found.
            LOG.error("getMessage() received a NPE with binding = {}", bindingName)
            return null
        }
    }

    private fun postAndVerifyProduct(compositeProduct: ProductAggregate, expectedStatus: HttpStatus) {

        client.post()
            .uri("/product-composite")
            .body(Mono.just(compositeProduct), ProductAggregate::class.java)
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