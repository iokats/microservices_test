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
import reactor.core.publisher.Mono.just
import com.ykatsatos.api.core.product.Product
import com.ykatsatos.microservices.core.product.persistence.ProductRepository


@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ProductRepository

    @BeforeEach
    fun setUp() {

        repository.deleteAll()
    }

    @Test
    fun getProductById() {

        // given
        val productId = 1

        // when
        postAndVerifyProduct(productId, OK)

        // then
        assertNotNull(repository.findByProductId(productId))
        getAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId)
    }

    @Test
    fun duplicateError() {

        // given
        val productId = 1

        // when
        postAndVerifyProduct(productId, OK)

        // then
        assertNotNull(repository.findByProductId(productId))
        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product")
            .jsonPath("$.message").isEqualTo("Duplicate key, Product id: $productId")
    }

    @Test
    fun deleteProduct() {

        // given
        val productId = 1

        postAndVerifyProduct(productId, OK)
        assertNotNull(repository.findByProductId(productId))

        // when
        deleteAndVerifyProduct(productId, OK)

        // then
        assertNull(repository.findByProductId(productId))
        deleteAndVerifyProduct(productId, OK)
    }

    @Test
    fun getProductInvalidParameterString() {

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
    fun getProductNotFound() {

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
    fun getProductInvalidParameterNegativeValue() {

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

    private fun postAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): WebTestClient.BodyContentSpec {

        val newProduct = Product(productId, "Name $productId", productId, "SA")

        return client.post()
            .uri("/product").
            body(just(newProduct), Product::class.java)
                .accept(APPLICATION_JSON).exchange()
                .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON).expectBody()
    }

    private fun deleteAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): WebTestClient.BodyContentSpec {

        return client.delete()
            .uri("/product/$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }
}