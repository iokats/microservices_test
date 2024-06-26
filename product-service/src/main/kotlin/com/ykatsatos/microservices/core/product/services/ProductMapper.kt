package com.ykatsatos.microservices.core.product.services

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import com.ykatsatos.api.core.product.Product
import com.ykatsatos.microservices.core.product.persistence.ProductEntity

@Mapper(componentModel = "spring")
interface ProductMapper {

    @Mappings(Mapping(target = "serviceAddress", ignore = true))
    fun entityToApi(entity: ProductEntity): Product

    @Mappings(Mapping(target = "id", ignore = true), Mapping(target = "version", ignore = true))
    fun apiToEntity(api: Product): ProductEntity
}