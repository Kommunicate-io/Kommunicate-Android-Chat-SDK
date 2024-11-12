package kommunicate.io.sample.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val apiClient: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.kommunicate.io")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val chatClient: Retrofit = Retrofit.Builder()
        .baseUrl("https://chat.kommunicate.io")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}