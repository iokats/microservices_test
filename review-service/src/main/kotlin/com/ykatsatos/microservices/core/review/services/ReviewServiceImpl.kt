package com.ykatsatos.microservices.core.review.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController
import com.ykatsatos.api.core.review.Review
import com.ykatsatos.api.core.review.ReviewService
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.microservices.core.review.persistence.ReviewEntity
import com.ykatsatos.microservices.core.review.persistence.ReviewRepository
import com.ykatsatos.microservices.utilities.http.ServiceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val LOG: Logger = LoggerFactory.getLogger(ReviewServiceImpl::class.java)

@RestController
class ReviewServiceImpl @Autowired constructor(
    private val repository: ReviewRepository,
    private val mapper: ReviewMapper,
    private val serviceUtil: ServiceUtil
): ReviewService {

    override suspend fun createReview(body: Review): Review {

        try {

            val reviewEntity = mapper.apiToEntity(body)

            val newReviewEntity = repository.save(reviewEntity)

            LOG.debug("createReview: created a review entity: ${body.productId}/${body.reviewId}")

            return mapper.entityToApi(newReviewEntity)

        } catch (dive: DataIntegrityViolationException) {

            throw InvalidInputException("Duplicate key, productId: ${body.productId}, reviewId: ${body.reviewId}")
        }
    }

    override fun getReviews(productId: Int): Flow<Review> {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        LOG.debug("getReviews")

        return repository.findByProductId(productId).map(this::entityToApi)
    }

    override suspend fun deleteReviews(productId: Int) {

        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: $productId")

        repository.deleteAll(repository.findByProductId(productId))
    }

    private fun entityToApi(reviewEntity: ReviewEntity): Review {

        val reviewApi = mapper.entityToApi(reviewEntity)

        return Review(
            reviewApi.productId,
            reviewApi.reviewId,
            reviewApi.author,
            reviewApi.subject,
            reviewApi.content,
            serviceUtil.serviceAddress
        )
    }
}