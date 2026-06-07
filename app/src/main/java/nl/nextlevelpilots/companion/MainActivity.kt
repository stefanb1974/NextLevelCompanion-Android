package nl.nextlevelpilots.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import nl.nextlevelpilots.companion.auth.AuthRepository
import nl.nextlevelpilots.companion.auth.SessionStore
import nl.nextlevelpilots.companion.navigation.AppNav
import nl.nextlevelpilots.companion.navigation.AppRoutes
import nl.nextlevelpilots.companion.ui.theme.NextLevelCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val sessionStore = remember { SessionStore(applicationContext) }
            var startDestination by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                val token = sessionStore.currentToken()
                startDestination = if (token != null) {
                    AppRoutes.DASHBOARD
                } else {
                    AppRoutes.LOGIN
                }
            }

            NextLevelCompanionTheme {
                when (startDestination) {
                    null -> SessionLoadingScreen()
                    else -> AppNav(
                        startDestination = startDestination!!,
                        sessionStore = sessionStore,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionLoadingScreen() {
    val gradientTop = Color(0xFF22287A)
    val gradientBottom = Color(0xFF3439A8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(gradientTop, gradientBottom),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (
        token: String,
        name: String?,
        email: String?,
        role: String?,
        linkedPersonId: String?,
    ) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    val gradientTop = Color(0xFF22287A)
    val gradientBottom = Color(0xFF3439A8)
    val accentOrange = Color(0xFFFF8B56)
    val lightGrey = Color(0xFFB8BCD4)
    val fieldBackground = Color(0xFF1A1F6B).copy(alpha = 0.55f)
    val glassBackground = Color.White.copy(alpha = 0.06f)
    val glassBorder = Color.White.copy(alpha = 0.12f)
    val errorBannerBackground = Color(0xFF0D1038).copy(alpha = 0.65f)
    val fieldShape = RoundedCornerShape(18.dp)
    val glassCardShape = RoundedCornerShape(28.dp)
    val logoCardShape = RoundedCornerShape(32.dp)
    val fieldBorder = Color.White.copy(alpha = 0.10f)

    val fieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White.copy(alpha = 0.6f),
        focusedContainerColor = fieldBackground,
        unfocusedContainerColor = fieldBackground,
        disabledContainerColor = fieldBackground.copy(alpha = 0.4f),
        cursorColor = accentOrange,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedPlaceholderColor = lightGrey,
        unfocusedPlaceholderColor = lightGrey,
        disabledPlaceholderColor = lightGrey.copy(alpha = 0.6f),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(gradientTop, gradientBottom),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(220.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.30f),
                                    Color.White.copy(alpha = 0.14f),
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.70f)
                        .height(160.dp)
                        .shadow(
                            elevation = 22.dp,
                            shape = logoCardShape,
                            ambientColor = Color.Black.copy(alpha = 0.28f),
                            spotColor = Color.Black.copy(alpha = 0.18f),
                        ),
                    shape = logoCardShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_nextlevel),
                            contentDescription = "NextLevel Pilots logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "NextLevel Pilots",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "COMPANION",
                color = lightGrey,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 5.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, glassBorder, glassCardShape),
                shape = glassCardShape,
                colors = CardDefaults.cardColors(containerColor = glassBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Inloggen",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(fieldShape)
                            .border(1.dp, fieldBorder, fieldShape),
                        placeholder = { Text("E-mail") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading,
                        singleLine = true,
                        shape = fieldShape,
                        colors = fieldColors,
                    )

                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(fieldShape)
                            .border(1.dp, fieldBorder, fieldShape),
                        placeholder = { Text("Wachtwoord") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isLoading,
                        singleLine = true,
                        shape = fieldShape,
                        colors = fieldColors,
                    )

                    if (errorMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(errorBannerBackground)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = accentOrange,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = errorMessage!!,
                                color = accentOrange,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null

                                when (val result = authRepository.login(email, password)) {
                                    is AuthRepository.LoginResult.Success -> {
                                        onLoginSuccess(
                                            result.token,
                                            result.name,
                                            result.email,
                                            result.role,
                                            result.linkedPersonId,
                                        )
                                    }

                                    is AuthRepository.LoginResult.Error -> {
                                        errorMessage = result.message
                                    }
                                }

                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentOrange,
                            contentColor = Color.White,
                            disabledContainerColor = accentOrange.copy(alpha = 0.72f),
                            disabledContentColor = Color.White.copy(alpha = 0.92f),
                        ),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "INLOGGEN",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Door in te loggen ga je akkoord met de algemene voorwaarden van Next Level Pilots.",
                color = lightGrey.copy(alpha = 0.92f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
