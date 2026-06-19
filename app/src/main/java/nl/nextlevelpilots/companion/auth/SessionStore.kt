package nl.nextlevelpilots.companion.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session",
)

class SessionStore(
    private val context: Context,
) {

    val tokenFlow: Flow<String?> = context.sessionDataStore.data.map { preferences ->
        preferences[KEY_TOKEN]
    }

    val userNameFlow: Flow<String?> = context.sessionDataStore.data.map { preferences ->
        preferences[KEY_USER_NAME]
    }

    val userEmailFlow: Flow<String?> = context.sessionDataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL]
    }

    val userRoleFlow: Flow<String?> = context.sessionDataStore.data.map { preferences ->
        preferences[KEY_USER_ROLE]
    }

    val linkedPersonIdFlow: Flow<String?> = context.sessionDataStore.data.map { preferences ->
        preferences[KEY_LINKED_PERSON_ID]
    }

    suspend fun saveSession(
        token: String,
        userName: String?,
        userEmail: String?,
        userRole: String?,
        linkedPersonId: String?,
    ) {
        context.sessionDataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
            if (!userName.isNullOrBlank()) {
                preferences[KEY_USER_NAME] = userName
            } else {
                preferences.remove(KEY_USER_NAME)
            }
            if (!userEmail.isNullOrBlank()) {
                preferences[KEY_USER_EMAIL] = userEmail
            } else {
                preferences.remove(KEY_USER_EMAIL)
            }
            if (!userRole.isNullOrBlank()) {
                preferences[KEY_USER_ROLE] = userRole
            } else {
                preferences.remove(KEY_USER_ROLE)
            }
            if (!linkedPersonId.isNullOrBlank()) {
                preferences[KEY_LINKED_PERSON_ID] = linkedPersonId
            } else {
                preferences.remove(KEY_LINKED_PERSON_ID)
            }
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun currentToken(): String? = tokenFlow.first()

    suspend fun currentLinkedPersonId(): String? = linkedPersonIdFlow.first()

    suspend fun currentUserRole(): String? = userRoleFlow.first()

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_LINKED_PERSON_ID = stringPreferencesKey("linked_person_id")
    }
}
