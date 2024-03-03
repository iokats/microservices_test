package se.magnus.microservices.core.recommendation.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import se.magnus.api.core.recommendation.Recommendation
import se.magnus.api.core.recommendation.RecommendationService
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.microservices.utilities.http.ServiceUtil

private val LOG: Logger = LoggerFactory.getLogger(RecommendationServiceImpl::class.java)

@RestController
class RecommendationServiceImpl @Autowired constructor(private val serviceUtil: ServiceUtil): RecommendationService {

    override fun getRecommendations(productId: Int): List<Recommendation> {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        if (productId == 113) {
            LOG.debug("No recommendations found for productId: $productId")
            return listOf()
        }

        val list: MutableList<Recommendation> = ArrayList()
        list.add(Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.serviceAddress))
        list.add(Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.serviceAddress))
        list.add(Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.serviceAddress))

        LOG.debug("/recommendation response size: ${list.size}", )

        return list
    }
}