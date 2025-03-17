package io.kommunicate.models

data class InQueueData(
    val status: String,
    val generatedAt: Long,
    val response: List<Long>
)
