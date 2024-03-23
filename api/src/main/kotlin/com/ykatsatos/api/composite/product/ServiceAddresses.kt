package com.ykatsatos.api.composite.product

data class ServiceAddresses(
    val compositeAddress: String,
    val productAddress: String,
    val reviewAddress: String,
    val recommendationAddress: String
)