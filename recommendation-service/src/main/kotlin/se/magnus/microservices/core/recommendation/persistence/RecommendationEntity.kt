package se.magnus.microservices.core.recommendation.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId': 1}")
class RecommendationEntity(
    var productId: Int,
    var recommendationId: Int,
    var author: String,
    var rating: Int,
    var content: String
) {
    @Id
    lateinit var id: String
        private set

    @Version
    var version: Int? = null
        private set
}