package se.magnus.microservices.core.review.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController
import se.magnus.api.core.review.Review
import se.magnus.api.core.review.ReviewService
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.microservices.core.review.persistence.ReviewEntity
import se.magnus.microservices.core.review.persistence.ReviewRepository
import se.magnus.microservices.utilities.http.ServiceUtil

private val LOG: Logger = LoggerFactory.getLogger(ReviewServiceImpl::class.java)

@RestController
class ReviewServiceImpl @Autowired constructor(
    private val repository: ReviewRepository,
    private val mapper: ReviewMapper,
    private val serviceUtil: ServiceUtil): ReviewService {

    override fun createReview(body: Review): Review {

        try {

            val reviewEntity = mapper.apiToEntity(body)

            val newReviewEntity = repository.save(reviewEntity)

            LOG.debug("createReview: created a review entity: ${body.productId}/${body.reviewId}")

            return mapper.entityToApi(newReviewEntity)

        } catch (dive: DataIntegrityViolationException) {

            throw InvalidInputException("Duplicate key, productId: ${body.productId}, reviewId: ${body.reviewId}")
        }
    }

    override fun getReviews(productId: Int): List<Review> {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        val reviewEntityList = repository.findByProductId(productId)

        val reviewApiList = entityListToApiList(reviewEntityList)

        LOG.debug("getReviews: response size: ${reviewApiList.size}")

        return reviewApiList
    }

    override fun deleteReviews(productId: Int) {

        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: $productId")

        repository.deleteAll(repository.findByProductId(productId))
    }

    private fun entityListToApiList(reviewEntityList: List<ReviewEntity>): List<Review> {

        val reviewApiList = mapper.entityListToApiList(reviewEntityList)

        return reviewApiList.map {
            Review(
                it.productId,
                it.reviewId,
                it.author,
                it.subject,
                it.content,
                serviceUtil.serviceAddress
            )
        }
    }
}