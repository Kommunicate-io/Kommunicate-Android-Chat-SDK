package kommunicate.io.sample.network

import com.google.gson.JsonObject
import kommunicate.io.sample.data.RegisterUserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KommunicateChatAPI {

    @GET("/rest/ws/user/v3/filter")
    suspend fun getUsers(
        @Header("X-Authorization") token: String,
        @Query("startIndex") startIndex: Int,
        @Query("pageSize") pageSize: Int,
        @Query("orderBy") orderBy: Int,
        @Query("roleNameList") roleNameList: String?,
        @Query("inactiveUser") inactiveUser: Boolean
    ): Response<RegisterUserResponse>

    @GET("rest/ws/message/list")
    suspend fun getMessageList(
        @Header("X-Authorization") token: String,
        @Query("startIndex") startIndex: Int,
        @Query("groupId") groupId: String,
        @Query("pageSize") pageSize: Int
    ): JsonObject
}