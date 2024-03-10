package se.magnus.microservices.core.review.persistence

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.Version

@Entity
@Table(
    name = "reviews",
    indexes = [Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")]
)
class ReviewEntity(
    @Id @GeneratedValue var id: Int,
    @Version var version: Int,
    var productId: Int,
    var reviewId: Int,
    var author: String,
    var subject: String,
    var content: String
)