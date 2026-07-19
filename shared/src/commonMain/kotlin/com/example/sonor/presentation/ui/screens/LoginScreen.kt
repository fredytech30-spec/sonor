package com.example.sonor.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.presentation.viewmodel.AuthViewModel
import com.example.sonor.presentation.viewmodel.AuthUiState
import com.example.sonor.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    // Cinematic Background Animation
    val infiniteTransition = rememberInfiniteTransition(label = "entrance")
    val orbit1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "orbit1"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
    ) {
        // Animated Aurora Orbs
        AuroraOrb(
            color = SonorPurpleCosmic.copy(alpha = 0.15f),
            size = 400.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(-150, -100) }
                .graphicsLayer { rotationZ = orbit1 }
        )
        AuroraOrb(
            color = SonorCyanElectric.copy(alpha = 0.12f),
            size = 500.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset { IntOffset(100, 150) }
                .graphicsLayer { rotationZ = -orbit1 }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Iconic "S" Logo with Glow
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, Brush.linearGradient(LuxuryGoldGradient), CircleShape),
                color = OnyxSurface,
                tonalElevation = 20.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "S",
                        style = Typography.displayLarge.copy(
                            fontSize = 58.sp,
                            brush = Brush.verticalGradient(LuxuryGoldGradient)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SONOR",
                style = Typography.displayLarge.copy(fontSize = 32.sp, letterSpacing = 8.sp),
                color = WhitePure,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphic Input Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.1f), Shapes.large),
                color = OnyxSurface.copy(alpha = 0.6f),
                shape = Shapes.large,
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLoginMode) "CONNEXION" else "INSCRIPTION",
                        style = Typography.titleMedium.copy(letterSpacing = 2.sp, color = SonorGoldLiquid),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-mail",
                        icon = Icons.Rounded.AlternateEmail
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mot de passe",
                        icon = Icons.Rounded.Lock,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Premium Action Button
                    Button(
                        onClick = { 
                            if (isLoginMode) {
                                viewModel.login(email, password)
                            } else {
                                viewModel.signUp(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(Shapes.medium),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(LuxuryGoldGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState is AuthUiState.Loading) {
                                CircularProgressIndicator(color = MidnightBlack, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    if (isLoginMode) "OUVRIR L'EXPÉRIENCE" else "CRÉER MON COMPTE",
                                    style = Typography.titleLarge.copy(
                                        fontSize = 15.sp, 
                                        color = MidnightBlack, 
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Switch Mode Text
                    Text(
                        text = if (isLoginMode) "Pas encore de compte ? S'inscrire" else "Déjà un compte ? Se connecter",
                        color = WhiteMuted,
                        style = Typography.bodyMedium,
                        modifier = Modifier.clickable { 
                            isLoginMode = !isLoginMode 
                            viewModel.clearError()
                        }
                    )
                }
            }

            // Authentication Errors
            AnimatedVisibility(
                visible = uiState is AuthUiState.Error,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val error = uiState as? AuthUiState.Error
                Surface(
                    modifier = Modifier.padding(top = 24.dp),
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = Shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = error?.message ?: "",
                        color = Color.Red,
                        style = Typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AuroraOrb(color: Color, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .blur(100.dp)
            .background(color, CircleShape)
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = WhiteMuted) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = SonorGoldLiquid) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        shape = Shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SonorGoldLiquid,
            unfocusedBorderColor = WhiteDim,
            cursorColor = SonorGoldLiquid,
            focusedLabelColor = SonorGoldLiquid,
            unfocusedLabelColor = WhiteMuted,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}
