package com.ykatsatos.microservices.core.product.services

import com.ykatsatos.api.core.product.Product
import com.ykatsatos.api.core.product.ProductService
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
class MessageProcessorConfig @Autowired constructor(private val productService: ProductService) {

    @Bean
    fun messageProcessor(): Consumer<Event<Int, Product>> {

        return Consumer { event ->
            LOG.info("Process message created at ${event.timestamp}...")

            when(event.eventType) {
                EventType.CREATE -> {
                    val product = event.data
                    LOG.info("Create product with ID: ${product?.productId}")
                    product?.let { runBlocking { productService.createProduct(product) } }
                }
                EventType.DELETE -> {
                    val productId = event.key
                    LOG.info("Delete product with ID: $productId")
                    runBlocking { productService.deleteProduct(productId) }
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