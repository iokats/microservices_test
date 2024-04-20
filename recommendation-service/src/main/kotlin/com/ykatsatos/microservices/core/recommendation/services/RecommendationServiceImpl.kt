package com.ykatsatos.microservices.core.recommendation.services

import com.mongodb.DuplicateKeyException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import com.ykatsatos.api.core.recommendation.Recommendation
import com.ykatsatos.api.core.recommendation.RecommendationService
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.microservices.core.recommendation.persistence.RecommendationEntity
import com.ykatsatos.microservices.core.recommendation.persistence.RecommendationRepository
import com.ykatsatos.microservices.utilities.http.ServiceUtil

private val LOG: Logger = LoggerFactory.getLogger(RecommendationServiceImpl::class.java)

@RestController
class RecommendationServiceImpl @Autowired constructor(
    private val repository: RecommendationRepository,
    private val mapper: RecommendationMapper,
    private val serviceUtil: ServiceUtil
): RecommendationService {

    override suspend fun createRecommendation(body: Recommendation): Recommendation {

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

    override suspend fun getRecommendations(productId: Int): List<Recommendation> {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        LOG.debug("getRecommendations")

        return repository.findByProductId(productId).map(this::entityToApi)
    }

    override suspend fun deleteRecommendations(productId: Int) {

        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: $productId")

        repository.deleteAll(repository.findByProductId(productId))
    }

    private fun entityToApi(recommendationEntity: RecommendationEntity): Recommendation {

        val recommendationApi = mapper.entityToApi(recommendationEntity)

        return Recommendation(
            recommendationApi.productId,
            recommendationApi.recommendationId,
            recommendationApi.author,
            recommendationApi.rate,
            recommendationApi.content,
            serviceUtil.serviceAddress
        )
    }
}