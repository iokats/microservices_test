package com.ykatsatos.microservices.core.product

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
import com.ykatsatos.api.core.product.Product
import com.ykatsatos.api.event.Event
import com.ykatsatos.api.event.EventType
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.microservices.core.product.persistence.ProductRepository
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import java.util.function.Consumer


@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["eureka.client.enabled=false"])
class ProductServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ProductRepository

    @Autowired
    @Qualifier("messageProcessor")
    private lateinit var messageProcessor: Consumer<Event<Int, Product>>

    @BeforeEach
    fun setUp() = runBlocking {

        repository.deleteAll()
    }

    @Test
    fun getProductById(): Unit = runBlocking  {

        // given
        val productId = 1

        // when
        sendCreateProductEvent(productId)

        // then
        assertNotNull(repository.findByProductId(productId))
        getAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId)
    }

    @Test
    fun duplicateError(): Unit = runBlocking {

        // given
        val productId = 1

        // when
        sendCreateProductEvent(productId)

        // then
        assertNotNull(repository.findByProductId(productId))
        val exception = assertThrows(InvalidInputException::class.java, {
            sendCreateProductEvent(productId)
        }, "Expected a InvalidInputException here!")
        assertEquals("Duplicate key, Product Id: $productId", exception.message)
    }

    @Test
    fun deleteProduct(): Unit = runBlocking {

        // given
        val productId = 2

        val product = sendCreateProductEvent(productId)
        assertNotNull(repository.findByProductId(productId))

        // when
        sendDeleteProductEvent(product)

        // then
        assertNull(repository.findByProductId(productId))
        sendDeleteProductEvent(product)
    }

    @Test
    fun getProductInvalidParameterString(): Unit = runBlocking {

        // given
        val productId = "no-integer"

        // when
        val response = getAndVerifyProduct("/$productId", BAD_REQUEST)

        //
        response
            .jsonPath("$.path").isEqualTo("/product/$productId")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getProductNotFound(): Unit = runBlocking {

        // given
        val productId = 13

        // when
        val response = getAndVerifyProduct(productId, NOT_FOUND)

        // then
        response
            .jsonPath("$.path").isEqualTo("/product/@$productId")
            .jsonPath("$.message").isEqualTo("No product found for productId: $productId")
    }

    @Test
    fun getProductInvalidParameterNegativeValue(): Unit = runBlocking {

        // given
        val productId = -1

        // when
        val response = getAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)

        // then
        response
            .jsonPath("$.path").isEqualTo("/product/@$productId")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productId")
    }

    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): WebTestClient.BodyContentSpec {

        return getAndVerifyProduct("/$productId", expectedStatus)
    }

    private fun getAndVerifyProduct(productIdPath: String, expectedStatus: HttpStatus): WebTestClient.BodyContentSpec {

        return client.get()
            .uri("/product$productIdPath")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun sendCreateProductEvent(productId: Int): Product {
        val product = Product(productId, "Name $productId", productId, "SA")
        val event = Event(EventType.CREATE, productId, product)
        messageProcessor.accept(event)
        return product
    }

    private fun sendDeleteProductEvent(product: Product) {
        val event: Event<Int, Product> = Event(EventType.DELETE, product.productId, product)
        messageProcessor.accept(event)
    }
}