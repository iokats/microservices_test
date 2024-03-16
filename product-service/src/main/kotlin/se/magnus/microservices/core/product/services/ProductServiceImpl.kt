package se.magnus.microservices.core.product.services

import com.mongodb.DuplicateKeyException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import se.magnus.api.core.product.Product
import se.magnus.api.core.product.ProductService
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.api.exceptions.NotFoundException
import se.magnus.microservices.core.product.persistence.ProductRepository
import se.magnus.microservices.utilities.http.ServiceUtil

private val LOG: Logger = LoggerFactory.getLogger(ProductServiceImpl::class.java)

@RestController
class ProductServiceImpl @Autowired constructor(
    private val repository: ProductRepository,
    private val mapper: ProductMapper,
    private val serviceUtil: ServiceUtil) : ProductService {

    override fun createProduct(body: Product): Product {

        try {
            val productEntity = mapper.apiToEntity(body)

            val newProductEntity = repository.save(productEntity)

            LOG.debug("createProduct: entity created for productId: ${body.productId}")

            return mapper.entityToApi(newProductEntity)

        } catch (dke: DuplicateKeyException) {

            throw InvalidInputException("Duplicate key, Product id: ${body.productId}")
        }
    }

    override fun getProduct(productId: Int): Product {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        val productEntity = repository.findByProductId(productId)
            ?: throw NotFoundException("No product found for productId: $productId")

        val response = mapper.entityToApi(productEntity)
        response.serviceAddress = serviceUtil.serviceAddress

        LOG.debug("getProduct: found productId: ${response.productId}")

        return response
    }

    override fun deleteProduct(productId: Int) {

        LOG.debug("deleteProduct: tries to delete an entity with productId: $productId")

        repository.findByProductId(productId)?.also(repository::delete)
    }
}
