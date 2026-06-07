package nl.nextlevelpilots.companion.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.BuildConfig
import nl.nextlevelpilots.companion.R
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign

private val DestructiveRed = Color(0xFFE53935)
private const val ABOUT_TEXT =
    "NextLevel Pilots helpt toekomstige airline piloten zich optimaal voor te bereiden op hun selectieproces en airline carrière. Van APS-MCC training tot het ontwikkelen van professionele hard en soft skills — allemaal gericht op een succesvolle start in de cockpit."

@Composable
fun ProfileScreen(
    userName: String?,
    userEmail: String?,
    userRole: String?,
    linkedPersonId: String?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userCode = userCodeFromSession(userName, linkedPersonId)
    val roleLabel = formatRoleLabel(userRole)
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    text = "Account verwijderen?",
                    color = CompanionDesign.Navy,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = "Neem contact op met NextLevel Pilots om je account definitief te verwijderen.",
                    color = CompanionDesign.TextPrimary,
                    lineHeight = 22.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("OK", color = CompanionDesign.Navy, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Annuleren", color = CompanionDesign.TextSecondary)
                }
            },
            containerColor = CompanionDesign.CardWhite,
            shape = CompanionDesign.CardShape,
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = CompanionDesign.ScreenPadding,
            vertical = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(CompanionDesign.SectionSpacing),
    ) {
        item {
            ProfileHeader(userCode = userCode)
        }

        item {
            ProfileUserSummary(
                userName = userName,
                userEmail = userEmail,
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LoginStatusCard()
                LogoutButton(onLogout = onLogout)
            }
        }

        item {
            ProfileInfoCard(
                userName = userName,
                userEmail = userEmail,
                roleLabel = roleLabel,
            )
        }

        item {
            AboutCard(
                versionLabel = "Versie ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            )
        }

        item {
            TextButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Privacybeleid",
                    color = CompanionDesign.Navy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        item {
            DeleteAccountButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier.padding(top = 32.dp),
            )
        }
    }
}

@Composable
private fun ProfileHeader(userCode: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_nextlevel),
            contentDescription = "NextLevel Pilots logo",
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .height(80.dp),
            contentScale = ContentScale.Fit,
        )

        Box(
            modifier = Modifier
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.06f),
                )
                .size(92.dp)
                .clip(CircleShape)
                .background(CompanionDesign.Navy),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = userCode,
                color = CompanionDesign.CardWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun ProfileUserSummary(
    userName: String?,
    userEmail: String?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = userName ?: "—",
            color = CompanionDesign.Navy,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = userEmail ?: "—",
            color = CompanionDesign.TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LogoutButton(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onLogout,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CompanionDesign.ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = CompanionDesign.Accent,
            contentColor = CompanionDesign.CardWhite,
        ),
    ) {
        Text(
            text = "Uitloggen",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DeleteAccountButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = CompanionDesign.ButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DestructiveRed,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, DestructiveRed),
    ) {
        Text(
            text = "Verwijder mijn account",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ProfileInfoCard(
    userName: String?,
    userEmail: String?,
    roleLabel: String?,
) {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CompanionDesign.CardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Profielinformatie",
                color = CompanionDesign.Navy,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            ProfileInfoRow(label = "Naam", value = userName ?: "—")
            ProfileInfoRow(label = "E-mail", value = userEmail ?: "—")
            ProfileInfoRow(label = "Rol", value = roleLabel ?: "—")
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = CompanionDesign.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            color = CompanionDesign.TextPrimary,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun LoginStatusCard() {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CompanionDesign.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.GppGood,
                contentDescription = null,
                tint = CompanionDesign.Success,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = "Ingelogd",
                color = CompanionDesign.Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AboutCard(
    versionLabel: String,
) {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CompanionDesign.CardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Over",
                color = CompanionDesign.Navy,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = ABOUT_TEXT,
                color = CompanionDesign.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
            Text(
                text = "www.nextlevelpilots.nl",
                color = CompanionDesign.Navy,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = versionLabel,
                color = CompanionDesign.TextSecondary,
                fontSize = 13.sp,
            )
        }
    }
}

private fun userCodeFromSession(
    userName: String?,
    linkedPersonId: String?,
): String {
    linkedPersonId
        ?.trim()
        ?.takeIf { it.length == 3 && it.all(Char::isLetter) }
        ?.uppercase()
        ?.let { return it }

    return userCodeFromName(userName)
}

private fun userCodeFromName(name: String?): String {
    if (name.isNullOrBlank()) return "???"

    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    if (parts.size < 2) {
        return parts.firstOrNull()?.take(3)?.uppercase()?.padEnd(3, '?') ?: "???"
    }

    val givenName = parts.first()
    val surname = parts.last()
    return buildString {
        append(surname.first().uppercaseChar())
        append(
            if (surname.length >= 2) {
                surname[1].uppercaseChar()
            } else {
                givenName.first().uppercaseChar()
            },
        )
        append(givenName.first().uppercaseChar())
    }
}

private fun formatRoleLabel(role: String?): String? {
    return when (role?.trim()?.lowercase()) {
        "student", "trainee" -> "Student"
        "instructor" -> "Instructeur"
        "admin" -> "Beheerder"
        null, "" -> null
        else -> role.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }
}
