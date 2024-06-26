package com.ykatsatos.api.composite.product

data class ProductAggregate(
    val productId: Int,
    val name: String,
    val weight: Int,
    val recommendations: List<RecommendationSummary>? = emptyList(),
    val reviews: List<ReviewSummary>? = emptyList(),
    val serviceAddresses: ServiceAddresses? = null
)
