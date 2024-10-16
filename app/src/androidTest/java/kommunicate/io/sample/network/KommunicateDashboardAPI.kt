package kommunicate.io.sample.network

import com.google.gson.JsonObject
import kommunicate.io.sample.data.RequestTokenData
import kommunicate.io.sample.data.TokenData
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface KommunicateDashboardAPI {

    @POST("rest/ws/loginv2")
    suspend fun login(
        @Body data: RequestTokenData
    ): TokenData

    @GET("rest/ws/settings/application/detail")
    suspend fun getUserSettings(
        @Header("Authorization") token: String
    ): JsonObject

    @PATCH("rest/ws/settings/application/detail")
    suspend fun updateUserSettings(
        @Header("Authorization") token: String,
        @Body jsonProperties: RequestBody
    ): JsonObject

    @GET("rest/ws/users")
    suspend fun getBotDetails(
        @Header("Authorization") token: String,
        @Query("type") type: Int = 2,
    ): JsonObject
}