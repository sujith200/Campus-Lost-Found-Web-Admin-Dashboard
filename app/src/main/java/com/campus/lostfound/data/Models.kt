package com.campus.lostfound.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
)

data class AuthResponse(val token: String, val user: UserDto)

data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String,
)

data class OwnerDto(
    val id: String,
    val displayName: String,
    val email: String? = null,
)

data class ItemDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val kind: String,
    val status: String,
    val imageUrl: String? = null,
    val owner: OwnerDto? = null,
    @SerializedName("matchScore") val matchScore: Float? = null,
)

data class ClaimUserDto(val id: String, val displayName: String)

data class ClaimDto(
    val id: String,
    val message: String,
    val status: String,
    val user: ClaimUserDto? = null,
)

data class ItemDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val kind: String,
    val status: String,
    val imageUrl: String? = null,
    val owner: OwnerDto? = null,
    val claims: List<ClaimDto>? = null,
)

data class MatchRequest(
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val kind: String,
)

data class MatchResponse(val results: List<ItemDto>)

data class CreateItemRequest(
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val kind: String,
    val imageUrl: String? = null,
)

data class CreateClaimRequest(val itemId: String, val message: String)

data class ChatRequest(val message: String)
data class ChatResponse(val reply: String, val source: String)
