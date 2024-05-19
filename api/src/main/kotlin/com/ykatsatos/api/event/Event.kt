package com.ykatsatos.api.event

import java.time.ZonedDateTime

data class Event<K, T>(
    val eventType: EventType,
    val key: K,
    val data: T? = null,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
)
