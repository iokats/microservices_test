package se.magnus.microservices.core.review.persistence

import jakarta.persistence.*

@Entity
@NoArg
@Table(
    name = "reviews",
    indexes = [Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")]
)
class ReviewEntity(
    var productId: Int,
    var reviewId: Int,
    var author: String,
    var subject: String,
    var content: String
) {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
        private set

    @Version
    var version: Int? = null
        private set
}