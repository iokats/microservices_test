package com.ykatsatos.api.core.review

data class Review(
    val productId: Int,
    val reviewId: Int,
    val author: String,
    val subject: String,
    val content: String,
    val serviceAddress: String? = null
)
