package com.ykatsatos.microservices.core.review.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository: CoroutineCrudRepository<ReviewEntity, Int> {

    fun findByProductId(productId: Int?): Flow<ReviewEntity>
}