package se.magnus.microservices.core.review.persistence

import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional
import se.magnus.microservices.core.review.persistence.ReviewEntity

interface ReviewRepository: CrudRepository<ReviewEntity, Int> {

    @Transactional(readOnly = true)
    fun findByProductId(productId: Int?): List<ReviewEntity>
}