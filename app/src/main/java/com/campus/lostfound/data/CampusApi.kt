package com.campus.lostfound.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CampusApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun me(@Header("Authorization") auth: String): Response<UserDto>

    @GET("api/items")
    suspend fun items(
        @Query("kind") kind: String? = null,
        @Query("q") q: String? = null,
    ): Response<List<ItemDto>>

    @GET("api/items/{id}")
    suspend fun item(@Path("id") id: String): Response<ItemDetailDto>

    @POST("api/items")
    suspend fun createItem(
        @Header("Authorization") auth: String,
        @Body body: CreateItemRequest,
    ): Response<ItemDto>

    @POST("api/items/match")
    suspend fun match(@Body body: MatchRequest): Response<MatchResponse>

    @POST("api/claims")
    suspend fun claim(
        @Header("Authorization") auth: String,
        @Body body: CreateClaimRequest,
    ): Response<ClaimDto>

    @POST("api/chat")
    suspend fun chat(
        @Header("Authorization") auth: String,
        @Body body: ChatRequest,
    ): Response<ChatResponse>
}
