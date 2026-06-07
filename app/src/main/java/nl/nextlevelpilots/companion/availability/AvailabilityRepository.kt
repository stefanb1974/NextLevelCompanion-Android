package nl.nextlevelpilots.companion.availability

import android.util.Log
import nl.nextlevelpilots.companion.auth.SessionStore
import nl.nextlevelpilots.companion.network.ApiClient
import java.time.LocalDate
import java.time.YearMonth

class AvailabilityRepository(
    private val sessionStore: SessionStore,
    private val availabilityApi: AvailabilityApi = ApiClient.availabilityApi,
) {

    sealed class LoadResult {
        data class Success(
            val availabilityByMonth: Map<String, Map<String, AvailabilityDayEntry>>,
        ) : LoadResult()

        data class Error(
            val technicalMessage: String,
            val userMessage: String = LOAD_ERROR_MESSAGE,
        ) : LoadResult()
    }

    sealed class SaveResult {
        data class Success(
            val availabilityByMonth: Map<String, Map<String, AvailabilityDayEntry>>,
            val savedDates: Set<LocalDate>,
        ) : SaveResult()

        data class Error(
            val technicalMessage: String,
            val userMessage: String = SAVE_ERROR_MESSAGE,
        ) : SaveResult()
    }

    suspend fun loadAvailability(month: YearMonth): LoadResult {
        val token = sessionStore.currentToken()
            ?: return LoadResult.Error(
                technicalMessage = "No auth token in session",
                userMessage = LOAD_ERROR_MESSAGE,
            )

        return try {
            val monthKey = month.toApiKey()
            val response = availabilityApi.getMyAvailability(
                authorization = bearer(token),
                month = monthKey,
            )
            val errorBodyRaw = if (!response.isSuccessful) response.errorBody()?.string() else null
            val body = response.body() ?: parseGetError(errorBodyRaw)

            logDebug("loadAvailability response (month=$monthKey, http=${response.code()}): ${bodyToJson(body)}")

            if (response.isSuccessful && body?.ok != false) {
                val availabilityByMonth = body?.data?.toAvailabilityByMonth(month).orEmpty()
                logDebug("loadAvailability normalized (month=$monthKey): ${bodyToJson(availabilityByMonth)}")
                logMappedDayStates(month, availabilityByMonth)

                LoadResult.Success(availabilityByMonth = availabilityByMonth)
            } else {
                val technicalMessage = body?.error ?: errorBodyRaw ?: "HTTP ${response.code()}"
                logBackendError("loadAvailability", technicalMessage, response.code())
                LoadResult.Error(
                    technicalMessage = technicalMessage,
                    userMessage = LOAD_ERROR_MESSAGE,
                )
            }
        } catch (e: Exception) {
            logBackendError("loadAvailability", e.message ?: e.toString())
            LoadResult.Error(
                technicalMessage = e.message ?: e.toString(),
                userMessage = LOAD_ERROR_MESSAGE,
            )
        }
    }

    suspend fun savePendingChanges(
        pendingChanges: Map<LocalDate, DayAvailability>,
        availabilityByMonth: Map<String, Map<String, AvailabilityDayEntry>>,
    ): SaveResult {
        if (pendingChanges.isEmpty()) {
            return SaveResult.Success(
                availabilityByMonth = availabilityByMonth,
                savedDates = emptySet(),
            )
        }

        val monthsToSave = pendingChanges.keys
            .map { YearMonth.from(it) }
            .distinct()
            .sorted()

        var mergedAvailability = availabilityByMonth

        for (month in monthsToSave) {
            val days = mergedDaysForMonth(
                month = month,
                pendingChanges = pendingChanges,
                availabilityByMonth = mergedAvailability,
            )
            when (val result = saveMonth(month = month, days = days)) {
                is SaveResult.Success -> {
                    mergedAvailability = mergeAvailabilityByMonth(
                        existing = mergedAvailability,
                        incoming = result.availabilityByMonth,
                    )
                }

                is SaveResult.Error -> return result
            }
        }

        return SaveResult.Success(
            availabilityByMonth = mergedAvailability,
            savedDates = pendingChanges.keys,
        )
    }

    fun mergedDaysForMonth(
        month: YearMonth,
        pendingChanges: Map<LocalDate, DayAvailability>,
        availabilityByMonth: Map<String, Map<String, AvailabilityDayEntry>>,
    ): Map<LocalDate, DayAvailability> {
        val merged = monthAvailabilityFrom(availabilityByMonth, month).toMutableMap()
        pendingChanges.forEach { (date, availability) ->
            if (YearMonth.from(date) != month) return@forEach
            if (availability.status == DayAvailabilityStatus.NOT_SET) {
                merged.remove(date)
            } else {
                merged[date] = availability
            }
        }
        return merged
    }

    suspend fun saveMonth(
        month: YearMonth,
        days: Map<LocalDate, DayAvailability>,
    ): SaveResult {
        val token = sessionStore.currentToken()
            ?: return SaveResult.Error(
                technicalMessage = "No auth token in session",
                userMessage = SAVE_ERROR_MESSAGE,
            )

        val availability = days
            .mapNotNull { (date, dayAvailability) ->
                dayAvailability.toDayEntry(date)?.let { date.toApiKey() to it }
            }
            .toMap()

        val request = SaveAvailabilityRequest(
            month = month.toApiKey(),
            availability = availability,
        )

        return try {
            logDebug("saveMonth request (month=${month.toApiKey()}): ${bodyToJson(request)}")

            val response = availabilityApi.saveMyAvailability(
                authorization = bearer(token),
                body = request,
            )
            val errorBodyRaw = if (!response.isSuccessful) response.errorBody()?.string() else null
            val body = response.body() ?: parseSaveError(errorBodyRaw)

            logDebug("saveMonth response (month=${month.toApiKey()}, http=${response.code()}): ${bodyToJson(body)}")

            if (response.isSuccessful && body?.ok != false) {
                val refreshed = body?.data?.toAvailabilityByMonth(month)
                val availabilityByMonth = if (!refreshed.isNullOrEmpty()) {
                    logDebug("saveMonth using POST body data (month=${month.toApiKey()}): ${bodyToJson(refreshed)}")
                    refreshed
                } else {
                    when (val reload = loadAvailability(month)) {
                        is LoadResult.Success -> {
                            logDebug("saveMonth reload after POST (month=${month.toApiKey()}): ${bodyToJson(reload.availabilityByMonth)}")
                            reload.availabilityByMonth
                        }

                        is LoadResult.Error -> {
                            logDebug("saveMonth reload failed, using optimistic local month (month=${month.toApiKey()})")
                            availabilityByMonthWithMonth(month = month, days = days)
                        }
                    }
                }

                SaveResult.Success(
                    availabilityByMonth = availabilityByMonth,
                    savedDates = days.keys,
                )
            } else {
                val technicalMessage = body?.error ?: errorBodyRaw ?: "HTTP ${response.code()}"
                logBackendError("saveMonth", technicalMessage, response.code())
                SaveResult.Error(
                    technicalMessage = technicalMessage,
                    userMessage = SAVE_ERROR_MESSAGE,
                )
            }
        } catch (e: Exception) {
            logBackendError("saveMonth", e.message ?: e.toString())
            SaveResult.Error(
                technicalMessage = e.message ?: e.toString(),
                userMessage = SAVE_ERROR_MESSAGE,
            )
        }
    }

    fun monthAvailabilityFrom(
        availabilityByMonth: Map<String, Map<String, AvailabilityDayEntry>>,
        month: YearMonth,
    ): Map<LocalDate, DayAvailability> {
        val monthKey = month.toApiKey()
        val monthData = availabilityByMonth[monthKey].orEmpty()

        return monthData.mapNotNull { (dateKey, entry) ->
            parseAvailabilityDate(dateKey, entry.date)?.let { date ->
                date to entry.toDayAvailability()
            }
        }.toMap()
    }

    fun mergeAvailabilityByMonth(
        existing: Map<String, Map<String, AvailabilityDayEntry>>,
        incoming: Map<String, Map<String, AvailabilityDayEntry>>,
    ): Map<String, Map<String, AvailabilityDayEntry>> {
        if (incoming.isEmpty()) return existing

        val result = existing.toMutableMap()
        incoming.forEach { (monthKey, incomingDays) ->
            val mergedDays = result[monthKey].orEmpty().toMutableMap()
            mergedDays.putAll(incomingDays)
            result[monthKey] = mergedDays
        }
        return result
    }

    fun replaceMonthsInCache(
        existing: Map<String, Map<String, AvailabilityDayEntry>>,
        incoming: Map<String, Map<String, AvailabilityDayEntry>>,
    ): Map<String, Map<String, AvailabilityDayEntry>> {
        if (incoming.isEmpty()) return existing
        return existing + incoming
    }

    fun logMappedDayStates(
        month: YearMonth,
        availabilityByMonth: Map<String, Map<String, AvailabilityDayEntry>>,
    ) {
        val mapped = monthAvailabilityFrom(availabilityByMonth, month)
        Log.d(TAG, "Mapped day states for ${month.toApiKey()} (${mapped.size} days):")
        mapped.toSortedMap().forEach { (date, availability) ->
            Log.d(TAG, "  $date -> ${availability.toDebugLabel()}")
        }
    }

    private fun availabilityByMonthWithMonth(
        month: YearMonth,
        days: Map<LocalDate, DayAvailability>,
    ): Map<String, Map<String, AvailabilityDayEntry>> {
        val monthKey = month.toApiKey()
        val monthAvailability = days
            .mapNotNull { (date, availability) ->
                availability.toDayEntry(date)?.let { date.toApiKey() to it }
            }
            .toMap()
        return mapOf(monthKey to monthAvailability)
    }

    private fun bearer(token: String): String = "Bearer $token"

    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }

    private fun logBackendError(
        operation: String,
        technicalMessage: String,
        httpCode: Int? = null,
    ) {
        val codeSuffix = httpCode?.let { " (HTTP $it)" }.orEmpty()
        Log.w(TAG, "$operation failed$codeSuffix: $technicalMessage")
    }

    private fun bodyToJson(value: Any?): String {
        if (value == null) return "null"
        return runCatching { ApiClient.gson.toJson(value) }.getOrElse { value.toString() }
    }

    companion object {
        private const val TAG = "AvailabilityRepository"
        const val LOAD_ERROR_MESSAGE = "Beschikbaarheid kon niet worden geladen."
        const val SAVE_ERROR_MESSAGE = "Beschikbaarheid kon niet worden opgeslagen."
        const val RELOAD_AFTER_SAVE_MESSAGE = "Opslaan gelukt, maar herladen mislukt"
    }

    private fun parseGetError(raw: String?): AvailabilityGetResponse? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            ApiClient.gson.fromJson(raw, AvailabilityGetResponse::class.java)
        }.getOrNull()
    }

    private fun parseSaveError(raw: String?): AvailabilitySaveResponse? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            ApiClient.gson.fromJson(raw, AvailabilitySaveResponse::class.java)
        }.getOrNull()
    }
}
