package com.ykatsatos.microservices.core.review.services

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import com.ykatsatos.api.core.review.Review
import com.ykatsatos.microservices.core.review.persistence.ReviewEntity

@Mapper(componentModel = "spring")
interface ReviewMapper {

    @Mappings(Mapping(target = "serviceAddress", ignore = true))
    fun entityToApi(entity: ReviewEntity): Review

    @Mappings(Mapping(target = "id", ignore = true), Mapping(target = "version", ignore = true))
    fun apiToEntity(api: Review): ReviewEntity

    fun entityListToApiList(entity: List<ReviewEntity>): List<Review>

    fun apiListToEntityList(entity: List<Review>): List<ReviewEntity>
}