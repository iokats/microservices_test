package com.ykatsatos.microservices.core.review.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository: CoroutineCrudRepository<ReviewEntity, Int> {

    suspend fun findByProductId(productId: Int?): List<ReviewEntity>
}