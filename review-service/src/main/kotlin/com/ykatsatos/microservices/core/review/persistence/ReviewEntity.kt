package com.ykatsatos.microservices.core.review.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table

@NoArg
@Table(name = "reviews")
class ReviewEntity(
    var productId: Int,
    var reviewId: Int,
    var author: String,
    var subject: String,
    var content: String
) {

    @Id
    var id: Int? = null
        private set

    @Version
    var version: Int? = null
        private set
}