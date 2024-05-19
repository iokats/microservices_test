package com.ykatsatos.microservices.core.recommendation.services

import com.ykatsatos.api.core.recommendation.Recommendation
import com.ykatsatos.api.core.recommendation.RecommendationService
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
class MessageProcessorConfig @Autowired constructor(private val recommendationService: RecommendationService) {

    @Bean
    fun messageProcessor(): Consumer<Event<Int, Recommendation>> {

        return Consumer { event ->
            LOG.info("Process message created at ${event.timestamp}...")

            when(event.eventType) {
                EventType.CREATE -> {
                    val recommendation = event.data
                    LOG.info("Create recommendation with ID: ${recommendation?.productId}/${recommendation?.recommendationId}")
                    recommendation?.let { runBlocking { recommendationService.createRecommendation(it) } }
                }
                EventType.DELETE -> {
                    val productId = event.key
                    LOG.info("Delete reviews with ProductID: $productId")
                    runBlocking { recommendationService.deleteRecommendations(productId) }
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