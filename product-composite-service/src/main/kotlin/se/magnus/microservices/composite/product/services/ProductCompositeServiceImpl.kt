package se.magnus.microservices.composite.product.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import se.magnus.api.composite.product.*
import se.magnus.api.core.product.Product
import se.magnus.api.core.recommendation.Recommendation
import se.magnus.api.core.review.Review
import se.magnus.microservices.utilities.http.ServiceUtil

private val LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl::class.java)

@RestController
class ProductCompositeServiceImpl @Autowired constructor(
    private val serviceUtil: ServiceUtil,
    private val integration: ProductCompositeIntegration
): ProductCompositeService {

    override fun createProduct(body: ProductAggregate) {

        try {
            LOG.debug("createCompositeProduct: creates a new composite entity for productId: ${body.productId}")

            val product = Product(body.productId, body.name, body.weight)
            integration.createProduct(product)

            body.recommendations.forEach { recommendationSummary ->
                val recommendation = Recommendation(
                    body.productId,
                    recommendationSummary.recommendationId,
                    recommendationSummary.author,
                    recommendationSummary.rate,
                    recommendationSummary.content
                )
                integration.createRecommendation(recommendation)
            }

            body.reviews.forEach { reviewSummary ->
                val review = Review(
                    body.productId,
                    reviewSummary.reviewId,
                    reviewSummary.author,
                    reviewSummary.subject,
                    reviewSummary.content
                )
                integration.createReview(review)
            }
        } catch (re: RuntimeException) {
            LOG.debug("createCompositeProduct failed $re")
            throw re
        }
    }

    override fun getProduct(productId: Int): ProductAggregate {

        val product = integration.getProduct(productId)

        val recommendations = integration.getRecommendations(productId)

        val reviews = integration.getReviews(productId)

        return createProductAggregate(product, recommendations, reviews, serviceUtil.serviceAddress)
    }

    override fun deleteProduct(productId: Int) {

        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: $productId")

        integration.deleteProduct(productId)

        integration.deleteRecommendations(productId)

        integration.deleteReviews(productId)

        LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: $productId")
    }

    private fun createProductAggregate(
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>,
        serviceAddress: String
    ): ProductAggregate {

        val recommendationSummaries = recommendations
            .map { RecommendationSummary(it.recommendationId, it.author, it.rate, it.content) }

        val reviewSummaries = reviews
            .map { ReviewSummary(it.reviewId, it.author, it.subject, it.content) }

        val serviceAddresses = createServiceAddresses(serviceAddress, product, recommendations, reviews)

        return ProductAggregate(
            product.productId,
            product.name,
            product.weight,
            recommendationSummaries,
            reviewSummaries,
            serviceAddresses
        )
    }

    private fun createServiceAddresses(
        serviceAddress: String,
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>
    ): ServiceAddresses = ServiceAddresses(
        serviceAddress,
        product.serviceAddress!!,
        if(reviews.isNotEmpty()) reviews[0].serviceAddress!! else "",
        if(recommendations.isNotEmpty()) recommendations[0].serviceAddress!! else "",
    )
}