package com.applozic.mobicomkit.uiwidgets.data

data class BusinessSettingsData(
    val generatedAt: Long,
    val response: List<BusinessSettingsResponse>,
    val status: String
)

data class BusinessSettingsResponse(
    val agentCount: Int,
    val botCount: Int,
    val businessHourMap: Map<Int, String>,
    val message: String,
    val teamId: Int,
    val teamMembers: List<Any>,
    val teamName: String,
    val timezone: String,
    val type: Int,
    val workingDays: String
)