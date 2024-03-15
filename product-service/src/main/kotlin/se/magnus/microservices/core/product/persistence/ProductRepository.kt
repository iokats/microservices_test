package se.magnus.microservices.core.product.persistence

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface ProductRepository: PagingAndSortingRepository<ProductEntity, String>, CrudRepository<ProductEntity, String> {

    fun findByProductId(productId: Int): ProductEntity?
}