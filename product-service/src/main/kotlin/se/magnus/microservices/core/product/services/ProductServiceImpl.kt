package se.magnus.microservices.core.product.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import se.magnus.api.core.product.Product
import se.magnus.api.core.product.ProductService
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.api.exceptions.NotFoundException
import se.magnus.microservices.utilities.http.ServiceUtil

private val LOG: Logger = LoggerFactory.getLogger(ProductServiceImpl::class.java)

@RestController
class ProductServiceImpl @Autowired constructor(private val serviceUtil: ServiceUtil) : ProductService {

    override fun getProduct(productId: Int): Product {

        LOG.debug("/product return the found product for productId={}", productId)

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        if (productId == 13) {
            throw NotFoundException("No product found for productId: $productId")
        }

        return Product(productId, "name-$productId", 123, serviceUtil.serviceAddress)
    }
}
