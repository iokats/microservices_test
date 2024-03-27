package com.ykatsatos.microservices.core.recommendation.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RecommendationRepository: CoroutineCrudRepository<RecommendationEntity, String> {

    fun findByProductId(productId: Int): Flow<RecommendationEntity>
}