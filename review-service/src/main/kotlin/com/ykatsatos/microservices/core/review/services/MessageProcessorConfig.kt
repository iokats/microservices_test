package com.ykatsatos.microservices.core.review.services

import com.ykatsatos.api.core.review.Review
import com.ykatsatos.api.core.review.ReviewService
import com.ykatsatos.api.event.Event
import com.ykatsatos.api.event.EventType
import com.ykatsatos.api.exceptions.EventProcessingException
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

private val LOG: Logger = LoggerFactory.getLogger(MessageProcessorConfig::class.java)

@Configuration
class MessageProcessorConfig @Autowired constructor(private val reviewService: ReviewService) {

    @Bean
    fun messageProcessor(): Consumer<Event<Int, Review>> {

        return Consumer { event ->
            LOG.info("Process message created at ${event.timestamp}...")

            when(event.eventType) {
                EventType.CREATE -> {
                    val review = event.data
                    LOG.info("Create review with ID: ${review?.productId}/${review?.reviewId}")
                    review?.let { runBlocking { reviewService.createReview(review) } }
                }
                EventType.DELETE -> {
                    val productId = event.key
                    LOG.info("Delete reviews with ProductID: $productId")
                    runBlocking { reviewService.deleteReviews(productId) }
                }
                else -> {
                    val errorMessage = "Incorrect event type ${event.eventType}"
                    LOG.warn(errorMessage)
                    throw EventProcessingException(errorMessage)
                }
            }
        }
    }
}