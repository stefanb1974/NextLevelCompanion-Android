package nl.nextlevelpilots.companion.auth

import nl.nextlevelpilots.companion.network.ApiClient

class AuthRepository(
    private val authApi: AuthApi = ApiClient.authApi,
) {

    sealed class LoginResult {
        data class Success(
            val token: String,
            val name: String?,
            val email: String?,
            val role: String?,
            val linkedPersonId: String?,
        ) : LoginResult()

        data class Error(val message: String) : LoginResult()
    }

    suspend fun login(email: String, password: String): LoginResult {
        if (email.isBlank()) {
            return LoginResult.Error("E-mail is verplicht.")
        }
        if (password.isBlank()) {
            return LoginResult.Error("Wachtwoord is verplicht.")
        }

        return try {
            val response = authApi.login(
                LoginRequest(
                    email = email.trim(),
                    password = password,
                )
            )
            val body = response.body() ?: parseErrorBody(response.errorBody()?.string())

            if (response.isSuccessful && body?.ok != false && body?.user != null) {
                val token = body.token
                if (token.isNullOrBlank()) {
                    return LoginResult.Error("Inloggen mislukt: geen token ontvangen.")
                }
                LoginResult.Success(
                    token = token,
                    name = body.user.name,
                    email = body.user.email,
                    role = body.user.role,
                    linkedPersonId = body.user.linkedPersonId,
                )
            } else {
                LoginResult.Error(
                    body?.error ?: "Inloggen mislukt (${response.code()})."
                )
            }
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Netwerkfout. Probeer het opnieuw.")
        }
    }

    private fun parseErrorBody(raw: String?): LoginResponse? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            ApiClient.gson.fromJson(raw, LoginResponse::class.java)
        }.getOrNull()
    }
}
