package com.ykatsatos.microservices.core.recommendation.services

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import com.ykatsatos.api.core.recommendation.Recommendation
import com.ykatsatos.microservices.core.recommendation.persistence.RecommendationEntity

@Mapper(componentModel = "spring")
interface RecommendationMapper {

    @Mappings(Mapping(target = "rate", source = "entity.rating"), Mapping(target = "serviceAddress", ignore = true))
    fun entityToApi(entity: RecommendationEntity): Recommendation

    @Mappings(
        Mapping(target = "rating", source = "api.rate"),
        Mapping(target = "id", ignore = true),
        Mapping(target = "version", ignore = true)
    )
    fun apiToEntity(api: Recommendation): RecommendationEntity

    fun entityListToApiList(entity: List<RecommendationEntity>): List<Recommendation>

    fun apiListToEntityList(entity: List<Recommendation>): List<RecommendationEntity>
}