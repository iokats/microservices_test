package com.ykatsatos.api.core.product

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface ProductService {

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/product \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body A JSON representation of the new product
     * @return A JSON representation of the newly created product
     */
    @PostMapping(value = ["/product"], consumes = ["application/json"], produces = ["application/json"])
    suspend fun createProduct(@RequestBody body: Product): Product

    /**
     * Sample usage: "curl $HOST:$PORT/product/1".
     *
     * @param productId ID of the product
     *  @return the product, if found, else null
     */
    @GetMapping(value = ["/product/{productId}"], produces = ["application/json"])
    suspend fun getProduct(@PathVariable productId: Int): Product

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product/1".
     *
     * @param productId ID of the product
     */
    @DeleteMapping(value = ["/product/{productId}"])
    suspend fun deleteProduct(@PathVariable productId: Int)
}