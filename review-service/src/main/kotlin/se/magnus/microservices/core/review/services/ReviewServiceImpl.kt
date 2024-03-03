package se.magnus.microservices.core.review.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import se.magnus.api.core.review.Review
import se.magnus.api.core.review.ReviewService
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.microservices.utilities.http.ServiceUtil

private val LOG: Logger = LoggerFactory.getLogger(ReviewServiceImpl::class.java)

@RestController
class ReviewServiceImpl @Autowired constructor(private val serviceUtil: ServiceUtil): ReviewService {

    override fun getReviews(productId: Int): List<Review> {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        if (productId == 213) {
            LOG.debug("No reviews found for productId: $productId")
            return listOf()
        }

        val list: MutableList<Review> = ArrayList()
        list.add(Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.serviceAddress))
        list.add(Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.serviceAddress))
        list.add(Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.serviceAddress))

        LOG.debug("/reviews response size: ${list.size}")

        return list
    }
}