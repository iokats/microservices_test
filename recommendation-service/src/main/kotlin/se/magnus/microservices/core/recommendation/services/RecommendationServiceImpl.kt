package se.magnus.microservices.core.recommendation.services

import com.mongodb.DuplicateKeyException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import se.magnus.api.core.recommendation.Recommendation
import se.magnus.api.core.recommendation.RecommendationService
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository
import se.magnus.microservices.utilities.http.ServiceUtil

private val LOG: Logger = LoggerFactory.getLogger(RecommendationServiceImpl::class.java)

@RestController
class RecommendationServiceImpl @Autowired constructor(
    private val repository: RecommendationRepository,
    private val mapper: RecommendationMapper,
    private val serviceUtil: ServiceUtil): RecommendationService {

    override fun createRecommendation(body: Recommendation): Recommendation {

        try {

            val recommendationEntity = mapper.apiToEntity(body)

            val newRecommendationEntity = repository.save(recommendationEntity)

            LOG.debug("createRecommendation: " +
                    "created a recommendation entity: ${body.productId}/${body.recommendationId}")

            return mapper.entityToApi(newRecommendationEntity)

        } catch (dke: DuplicateKeyException) {

            throw InvalidInputException("Duplicate key, " +
                    "productId: ${body.productId}, recommendationId: ${body.recommendationId}")
        }
    }

    override fun getRecommendations(productId: Int): List<Recommendation> {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }
        val recommendationEntityList = repository.findByProductId(productId)

        val recommendationApiList = mapper.entityListToApiList(recommendationEntityList)

        recommendationApiList.forEach { it.serviceAddress = serviceUtil.serviceAddress }

        LOG.debug("getRecommendations: response size: ${recommendationApiList.size}", )

        return recommendationApiList
    }

    override fun deleteRecommendations(productId: Int) {

        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: $productId")

        repository.deleteAll(repository.findByProductId(productId))
    }
}