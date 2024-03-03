package se.magnus.api.core.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

interface ProductService {

    /**
     * Sample usage: "curl $HOST:$PORT/product/1".
     *
     * @param productId Id of the product
     *  @return the product, if found, else null
     */
    @GetMapping(value = ["/product/{productId}"], produces = ["application/json"])
    fun getProduct(@PathVariable productId: Int): Product
}