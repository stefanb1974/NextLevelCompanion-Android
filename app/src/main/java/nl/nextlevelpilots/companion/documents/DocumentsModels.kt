package nl.nextlevelpilots.companion.documents

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import nl.nextlevelpilots.companion.network.ApiClient
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DocumentsResponse(
    val ok: Boolean? = null,
    val data: List<DocumentDto>? = null,
    val documents: List<DocumentDto>? = null,
    val error: String? = null,
) {
    fun documentEntries(): List<DocumentDto> = data ?: documents ?: emptyList()
}

fun parseDocumentsResponse(raw: String?): DocumentsResponse? {
    if (raw.isNullOrBlank()) return null

    return runCatching {
        val element = ApiClient.gson.fromJson(raw, JsonElement::class.java)
        when {
            element.isJsonArray -> {
                DocumentsResponse(
                    ok = true,
                    data = element.asJsonArray.mapNotNull { it.toDocumentDto() },
                )
            }

            element.isJsonObject -> {
                val obj = element.asJsonObject
                DocumentsResponse(
                    ok = parseOkFlag(obj.get("ok")),
                    data = obj.parseDocumentList("data"),
                    documents = obj.parseDocumentList("documents"),
                    error = obj.get("error")?.takeUnless { it.isJsonNull }?.asString,
                )
            }

            else -> null
        }
    }.getOrNull()
}

private fun JsonObject.parseDocumentList(key: String): List<DocumentDto>? {
    val array = get(key)?.takeUnless { it.isJsonNull } ?: return null
    if (!array.isJsonArray) return null
    return array.asJsonArray.mapNotNull { it.toDocumentDto() }
}

private fun parseOkFlag(element: JsonElement?): Boolean? {
    if (element == null || element.isJsonNull) return null
    return when {
        element.isJsonPrimitive && element.asJsonPrimitive.isBoolean -> element.asBoolean
        element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> element.asInt != 0
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
            element.asString.equals("true", ignoreCase = true) || element.asString == "1"
        }
        else -> null
    }
}

private fun JsonElement.toDocumentDto(): DocumentDto? {
    if (!isJsonObject) return null
    val obj = asJsonObject
    return DocumentDto(
        id = obj.stringValue("id"),
        name = obj.stringValue("name"),
        title = obj.stringValue("title"),
        description = obj.stringValue("description"),
        type = obj.stringValue("type"),
        status = obj.stringValue("status"),
        isEasa = obj.booleanValue("is_easa"),
        updatedAt = obj.stringValue("updated_at"),
        updatedAtCamel = obj.stringValue("updatedAt"),
        readAt = obj.stringValue("read_at"),
        isRead = obj.booleanValue("is_read"),
        mimeType = obj.stringValue("mime_type"),
    )
}

private fun JsonObject.stringValue(key: String): String? {
    val element = get(key) ?: return null
    if (element.isJsonNull) return null
    return when {
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> element.asString
        element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> element.asJsonPrimitive.asString
        element.isJsonPrimitive && element.asJsonPrimitive.isBoolean -> element.asBoolean.toString()
        else -> null
    }?.takeIf { it.isNotBlank() }
}

private fun JsonObject.booleanValue(key: String): Boolean? {
    val element = get(key) ?: return null
    if (element.isJsonNull) return null
    return when {
        element.isJsonPrimitive && element.asJsonPrimitive.isBoolean -> element.asBoolean
        element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> element.asInt != 0
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
            element.asString.equals("true", ignoreCase = true) || element.asString == "1"
        }
        else -> null
    }
}

data class DocumentDto(
    val id: String? = null,
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val status: String? = null,
    @SerializedName("is_easa")
    val isEasa: Boolean? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAtCamel: String? = null,
    @SerializedName("read_at")
    val readAt: String? = null,
    @SerializedName("is_read")
    val isRead: Boolean? = null,
    @SerializedName("mime_type")
    val mimeType: String? = null,
)

data class DocumentUiModel(
    val id: String,
    val title: String,
    val typeLabel: String?,
    val statusLabel: String?,
    val updatedAtLabel: String?,
    val isEasa: Boolean,
    val isRead: Boolean,
)

fun DocumentDto.toDocumentUiModel(): DocumentUiModel? {
    val documentId = id?.takeIf { it.isNotBlank() } ?: return null
    val documentTitle = name?.takeIf { it.isNotBlank() }
        ?: title?.takeIf { it.isNotBlank() }
        ?: "Document"

    return DocumentUiModel(
        id = documentId,
        title = documentTitle,
        typeLabel = type?.takeIf { it.isNotBlank() }?.let(::formatDocumentTypeLabel),
        statusLabel = status?.takeIf { it.isNotBlank() }?.let(::formatDocumentStatusLabel),
        updatedAtLabel = resolveUpdatedAtLabel(updatedAt ?: updatedAtCamel),
        isEasa = isEasa == true,
        isRead = isRead == true || !readAt.isNullOrBlank(),
    )
}

fun formatDocumentTypeLabel(raw: String): String {
    return when (raw.trim().lowercase()) {
        "syllabus" -> "Syllabus"
        "manual" -> "Handleiding"
        "checklist" -> "Checklist"
        "form" -> "Formulier"
        "report" -> "Rapport"
        else -> raw.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.forLanguageTag("nl-NL")) else char.toString()
        }
    }
}

fun formatDocumentStatusLabel(raw: String): String {
    return when (raw.trim().lowercase()) {
        "approved" -> "Goedgekeurd"
        "draft" -> "Concept"
        "pending" -> "In behandeling"
        "archived" -> "Gearchiveerd"
        "rejected" -> "Afgekeurd"
        else -> raw.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.forLanguageTag("nl-NL")) else char.toString()
        }
    }
}

private fun resolveUpdatedAtLabel(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return runCatching {
        val date = when {
            raw.contains('T') -> OffsetDateTime.parse(raw).toLocalDate()
            else -> LocalDate.parse(raw.take(10))
        }
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("nl-NL"))
        date.format(formatter)
    }.getOrNull()
}
