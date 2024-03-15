package se.magnus.microservices.core.product.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import kotlin.properties.Delegates

@Document(collection = "products")
class ProductEntity(
    @Indexed(unique = true) var productId: Int,
    var name: String,
    var weight: Int
) {
   @Id
   lateinit var id: String
       private set

   @Version
   var version: Int? = null
       private set
}