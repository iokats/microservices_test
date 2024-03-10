package se.magnus.microservices.core.product.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "products")
class ProductEntity(
    @Id var id: String,
    @Version var version: Int,
    @Indexed(unique = true) var productId: Int,
    var name: String,
    var weight: Int
)