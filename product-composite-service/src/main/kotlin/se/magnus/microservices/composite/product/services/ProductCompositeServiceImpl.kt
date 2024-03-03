package se.magnus.microservices.composite.product.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import se.magnus.api.composite.product.*
import se.magnus.api.core.product.Product
import se.magnus.api.core.recommendation.Recommendation
import se.magnus.api.core.review.Review
import se.magnus.microservices.utilities.http.ServiceUtil


@RestController
class ProductCompositeServiceImpl @Autowired constructor(
    private val serviceUtil: ServiceUtil,
    private val integration: ProductCompositeIntegration
): ProductCompositeService {

    override fun getProduct(productId: Int): ProductAggregate {

        val product = integration.getProduct(productId)

        val recommendations = integration.getRecommendations(productId)

        val reviews = integration.getReviews(productId)

        return createProductAggregate(product, recommendations, reviews, serviceUtil.serviceAddress)
    }

    private fun createProductAggregate(
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>,
        serviceAddress: String
    ): ProductAggregate {

        val recommendationSummaries = recommendations.map { RecommendationSummary(it.recommendationId, it.author, it.rate) }
        val reviewSummaries = reviews.map { ReviewSummary(it.reviewId, it.author, it.subject) }

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
        product.serviceAddress,
        if(reviews.isNotEmpty()) reviews[0].serviceAddress else "",
        if(recommendations.isNotEmpty()) recommendations[0].serviceAddress else "",
    )
}