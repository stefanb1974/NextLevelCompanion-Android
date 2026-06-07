package nl.nextlevelpilots.companion.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val ok: Boolean? = null,
    val token: String? = null,
    val user: AuthUser? = null,
    val error: String? = null,
)

data class AuthUser(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    @SerializedName("linked_person_id")
    val linkedPersonId: String? = null,
)
