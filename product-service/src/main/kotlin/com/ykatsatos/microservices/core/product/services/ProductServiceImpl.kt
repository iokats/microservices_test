package com.ykatsatos.microservices.core.product.services

import com.mongodb.DuplicateKeyException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import com.ykatsatos.api.core.product.Product
import com.ykatsatos.api.core.product.ProductService
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.api.exceptions.NotFoundException
import com.ykatsatos.microservices.core.product.persistence.ProductEntity
import com.ykatsatos.microservices.core.product.persistence.ProductRepository
import com.ykatsatos.microservices.utilities.http.ServiceUtil

private val LOG: Logger = LoggerFactory.getLogger(ProductServiceImpl::class.java)

@RestController
class ProductServiceImpl @Autowired constructor(
    private val repository: ProductRepository,
    private val mapper: ProductMapper,
    private val serviceUtil: ServiceUtil
) : ProductService {

    override suspend fun createProduct(body: Product): Product {

        try {
            val productEntity = mapper.apiToEntity(body)

            val newProductEntity = repository.save(productEntity)

            LOG.debug("createProduct: entity created for productId: ${body.productId}")

            return mapper.entityToApi(newProductEntity)

        } catch (dke: DuplicateKeyException) {

            throw InvalidInputException("Duplicate key, Product id: ${body.productId}")
        }
    }

    override suspend fun getProduct(productId: Int): Product {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        val productEntity = repository.findByProductId(productId)
            ?: throw NotFoundException("No product found for productId: $productId")

        val response = entityToApi(productEntity)

        LOG.debug("getProduct: found productId: ${response.productId}")

        return response
    }

    override suspend fun deleteProduct(productId: Int) {

        LOG.debug("deleteProduct: tries to delete an entity with productId: $productId")

        repository.findByProductId(productId)?.also{ productEntity -> repository.delete(productEntity) }
    }

    private fun entityToApi(productEntity: ProductEntity): Product {

        val response = mapper.entityToApi(productEntity)

        return Product(response.productId, response.name, response.weight, serviceUtil.serviceAddress)
    }
}
