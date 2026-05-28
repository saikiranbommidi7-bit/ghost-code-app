package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.GhostScreen
import com.example.viewmodel.GhostViewModel
import com.example.viewmodel.NotificationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// CENTRAL HUB SCREEN ROUTER
// ==========================================
@Composable
fun GhostAppContent(viewModel: GhostViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profileState by viewModel.profileState.collectAsState()

    // Screen selection based on state
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
    ) {
        when (currentScreen) {
            GhostScreen.Splash -> SplashScreen(viewModel)
            GhostScreen.Login -> LoginScreen(viewModel)
            GhostScreen.Signup -> SignupScreen(viewModel)
            else -> {
                // If not logged in, enforce login screen (except splash)
                if (!profileState.isLoggedIn) {
                    LaunchedEffect(Unit) {
                        viewModel.navigateTo(GhostScreen.Login)
                    }
                }

                Scaffold(
                    bottomBar = {
                        GhostBottomNavigation(
                            activeScreen = currentScreen,
                            onTabSelected = { targetScreen ->
                                viewModel.navigateTo(targetScreen)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    containerColor = GhostDarkBg
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith
                                        fadeOut(animationSpec = tween(220))
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                GhostScreen.Home -> HomeDashboard(viewModel)
                                GhostScreen.PathDetail -> PathProgressScreen(viewModel)
                                GhostScreen.LessonDetail -> LessonDetailsScreen(viewModel)
                                GhostScreen.Practice -> PracticeSandboxScreen(viewModel)
                                GhostScreen.Search -> SearchScreen(viewModel)
                                GhostScreen.Profile -> ProfileScreen(viewModel)
                                GhostScreen.Notifications -> NotificationsScreen(viewModel)
                                GhostScreen.Settings -> SettingsScreen(viewModel)
                                else -> HomeDashboard(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. WELCOME SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(viewModel: GhostViewModel) {
    val profile by viewModel.profileState.collectAsState()
    var alphaVal by remember { mutableStateOf(0f) }
    var progressVal by remember { mutableStateOf(0f) }

    val logoAlpha by animateFloatAsState(
        targetValue = alphaVal,
        animationSpec = tween(1000, easing = LinearOutSlowInEasing),
        label = "logo_fade"
    )

    val loadProgress by animateFloatAsState(
        targetValue = progressVal,
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "progress_bar"
    )

    LaunchedEffect(Unit) {
        alphaVal = 1f
        progressVal = 1f
        delay(2200) // Beautiful cinematic duration
        if (profile.isLoggedIn) {
            viewModel.navigateTo(GhostScreen.Home)
        } else {
            viewModel.navigateTo(GhostScreen.Login)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GhostDarkBg, Color(0xFF0F0B1E), GhostDarkBg)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant glowing abstract avatar representing the Ghost Code mascot
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(PurplePrimary.copy(alpha = 0.5f * logoAlpha), Color.Transparent),
                                center = center,
                                radius = size.minDimension / 1.1f
                            )
                        )
                    }
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PurplePrimary, IndigoPrimary)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👻",
                    fontSize = 54.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "GHOST CODE",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 6.sp,
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "friendly code spellbook & sandbox",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = CyanAccent,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))

            // Progress loader
            Column(
                modifier = Modifier.width(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(GhostBorder, shape = CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(loadProgress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PurplePrimary, CyanAccent)
                                ),
                                shape = CircleShape
                            )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Compiling Spectres ${ (loadProgress * 100).toInt() }%",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ==========================================
// 2. INTERACTIVE LOGIN & SIGNUP SCREENS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: GhostViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Hero title
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.linearGradient(colors = listOf(PurplePrimary, IndigoPrimary)),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👻", fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome Back, Ghost",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "The dark coding terminal awaits your instructions.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Form Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GhostCardBg, shape = RoundedCornerShape(24.dp))
                .border(1.dp, GhostBorder, shape = RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "LOG IN SECURELY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary,
                    letterSpacing = 1.5.sp
                )

                // Email
                Column {
                    Text(text = "EMAIL ADDRESS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = viewModel.loginEmail,
                        onValueChange = { viewModel.loginEmail = it },
                        placeholder = { Text("e.g. casper@code.dev", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = GhostBorder,
                            focusedContainerColor = GhostDarkBg,
                            unfocusedContainerColor = GhostDarkBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                // Password
                Column {
                    Text(text = "ACCUMULATOR PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = viewModel.loginPassword,
                        onValueChange = { viewModel.loginPassword = it },
                        placeholder = { Text("Enter terminal password", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = GhostBorder,
                            focusedContainerColor = GhostDarkBg,
                            unfocusedContainerColor = GhostDarkBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                // Error box
                viewModel.authError?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF451A1A), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFF43F5E), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = err, color = Color(0xFFFDA4AF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Log In Button
                Button(
                    onClick = { viewModel.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Initialize Spellbook",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { viewModel.navigateTo(GhostScreen.Signup) }
        ) {
            Text(text = "Apprentice new here? ", color = TextSecondary, fontSize = 13.sp)
            Text(text = "Sprout a profile 🚀", color = PurplePrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(viewModel: GhostViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.linearGradient(colors = listOf(CyanAccent, IndigoPrimary)),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✨", fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Generate Apprentice ID",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Claim your friendly spellbook to track algorithms, levels & streaks.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Form Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GhostCardBg, shape = RoundedCornerShape(24.dp))
                .border(1.dp, GhostBorder, shape = RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "APPRENTICE SPECIFICATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.5.sp
                )

                // Username
                Column {
                    Text(text = "CODENAME / USERNAME", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = viewModel.signupUsername,
                        onValueChange = { viewModel.signupUsername = it },
                        placeholder = { Text("e.g. CasperCoder", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth().testTag("signup_username_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = GhostBorder,
                            focusedContainerColor = GhostDarkBg,
                            unfocusedContainerColor = GhostDarkBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                // Email
                Column {
                    Text(text = "EMAIL ADDRESS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = viewModel.signupEmail,
                        onValueChange = { viewModel.signupEmail = it },
                        placeholder = { Text("e.g. yourname@domain.com", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = GhostBorder,
                            focusedContainerColor = GhostDarkBg,
                            unfocusedContainerColor = GhostDarkBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                // Password
                Column {
                    Text(text = "SECRET SIGNATURE (PASSWORD)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = viewModel.signupPassword,
                        onValueChange = { viewModel.signupPassword = it },
                        placeholder = { Text("Minimum 4 tokens", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = GhostBorder,
                            focusedContainerColor = GhostDarkBg,
                            unfocusedContainerColor = GhostDarkBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                // Error Box
                viewModel.authError?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF451A1A), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFF43F5E), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = err, color = Color(0xFFFDA4AF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sign Up Button
                Button(
                    onClick = { viewModel.signup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_signup_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Materialize Profile",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { viewModel.navigateTo(GhostScreen.Login) }
        ) {
            Text(text = "Already initiated? ", color = TextSecondary, fontSize = 13.sp)
            Text(text = "Access Terminal ➔", color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// 3. HOME SCREEN DASHBOARD
// ==========================================
@Composable
fun HomeDashboard(viewModel: GhostViewModel) {
    val profile by viewModel.profileState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
            .verticalScroll(scrollState)
            .padding(top = 16.dp, bottom = 40.dp)
    ) {
        // Mock Status Bar / Header Bar
        HeaderBar(viewModel)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // Gamified Stats Row (MATCHES THE DESIGN DIRECTIVE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GhostBorder, RoundedCornerShape(20.dp)),
                    color = GhostButtonBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔥", fontSize = 24.sp, modifier = Modifier.padding(bottom = 2.dp))
                        Text(text = "${profile.streakDays} Days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Streak Fire", fontSize = 9.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                    }
                }

                // Level Card with glow ring
                Surface(
                    modifier = Modifier
                        .weight(1.1f)
                        .border(1.dp, PurplePrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                    color = GhostButtonBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "✨", fontSize = 24.sp, modifier = Modifier.padding(bottom = 2.dp))
                        Text(text = "Level ${profile.level}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                        Text(text = "Apprentice Scholar", fontSize = 9.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                    }
                }

                // XP Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GhostBorder, RoundedCornerShape(20.dp)),
                    color = GhostButtonBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "💎", fontSize = 24.sp, modifier = Modifier.padding(bottom = 2.dp))
                        Text(text = "${profile.xp} XP", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Total Power", fontSize = 9.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hero Learning Card (Python Basics Selected default)
            val path = viewModel.coursePaths.first()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(28.dp)
                    )
                    .drawBehind {
                        // Ambient dynamic blur light
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(PurplePrimary.copy(alpha = 0.15f), CyanAccent.copy(alpha = 0.08f))
                            ),
                            cornerRadius = CornerRadius(28.dp.toPx())
                        )
                    }
                    .background(Color(0xFF111111), shape = RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                // Giant italic watermark numbering
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "01",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary.copy(alpha = 0.04f)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Current Path badge
                    Box(
                        modifier = Modifier
                            .background(PurplePrimary.copy(alpha = 0.12f), shape = CircleShape)
                            .border(1.dp, PurplePrimary.copy(alpha = 0.25f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CURRENT PATH",
                            fontSize = 9.sp,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Column {
                        Text(
                            text = path.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Learning variables, recursive stacks & loops",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Progress slider
                    val solvedRatio = if (path.lessons.isNotEmpty()) {
                        val completedCount = path.lessons.count { profile.completedLessonIds.contains(it.id) }
                        completedCount.toFloat() / path.lessons.size
                    } else 0f

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(text = "Course Progress", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${(solvedRatio * 100).toInt()}%",
                                color = PurplePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Custom progress track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color(0xFF1E1E1E), shape = CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (solvedRatio > 0) solvedRatio else 0.05f) // min loading showing
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(PurplePrimary, IndigoPrimary)
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.selectPath(path) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("continue_mission_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Continue Mission", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🚀", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Daily Challenges Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "THE DAILY HUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "View All",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary,
                    modifier = Modifier.clickable { viewModel.navigateTo(GhostScreen.Search) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Challenges List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                viewModel.challenges.forEach { challenge ->
                    val isSolved = profile.solvedChallengeIds.contains(challenge.id)
                    ChallengeCard(challenge = challenge, isSolved = isSolved) {
                        viewModel.selectChallenge(challenge)
                    }
                }
            }
        }
    }
}

// ==========================================
// SHARED WIDGETS
// ==========================================
@Composable
fun HeaderBar(viewModel: GhostViewModel) {
    val profile by viewModel.profileState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(colors = listOf(PurplePrimary, IndigoPrimary)),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👻", fontSize = 20.sp)
            }

            Column {
                Text(
                    text = "Ghost Code",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF10B981), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE: PYTHON SPECTRES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Notification bell
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(GhostSubCardBg, shape = RoundedCornerShape(14.dp))
                    .border(1.dp, GhostBorder, shape = RoundedCornerShape(14.dp))
                    .clickable { viewModel.navigateTo(GhostScreen.Notifications) },
                contentAlignment = Alignment.Center
            ) {
                Box {
                    Text(text = "🔔", fontSize = 18.sp)
                    // Notification bubble indicator if unread exist
                    val unread by viewModel.notifications.collectAsState()
                    if (unread.any { !it.isRead }) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, shape = CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            // Profile Avatar Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        Brush.linearGradient(colors = listOf(Color(0xFF475569), Color(0xFF94A3B8))),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, GhostBorder, shape = RoundedCornerShape(14.dp))
                    .clickable { viewModel.navigateTo(GhostScreen.Profile) }
            )
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge, isSolved: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GhostSubCardBg, shape = RoundedCornerShape(22.dp))
            .border(1.dp, GhostBorder, shape = RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box depending on difficulty
        val difficultyColor = when (challenge.difficulty) {
            "Easy" -> CyanAccent
            "Medium" -> OrangeAccent
            else -> PurplePrimary
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .background(difficultyColor.copy(alpha = 0.1f), shape = RoundedCornerShape(14.dp))
                .border(1.dp, difficultyColor.copy(alpha = 0.2f), shape = RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = challenge.emoji, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = challenge.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "${challenge.category} • ${challenge.difficulty}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+${challenge.xpReward} XP",
                    fontSize = 11.sp,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Completion indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, if (isSolved) EmeraldAccent.copy(alpha = 0.35f) else GhostBorder, CircleShape)
                .background(if (isSolved) EmeraldAccent.copy(alpha = 0.12f) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = if (isSolved) "Solved" else "Arrow",
                tint = if (isSolved) EmeraldAccent else TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ==========================================
// 4. PATH / TIMELINE PROGRESS SCREEN
// ==========================================
@Composable
fun PathProgressScreen(viewModel: GhostViewModel) {
    val selectedPath by viewModel.selectedPath.collectAsState()
    val profile by viewModel.profileState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
    ) {
        // Simple Top Nav Back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo(GhostScreen.Home) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "COURSE DETAIL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiary,
                letterSpacing = 1.5.sp
            )
            IconButton(onClick = { }, enabled = false) {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Path Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(GhostSubCardBg, GhostDarkBg)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(1.dp, GhostBorder, shape = RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PurplePrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = selectedPath.emoji, fontSize = 18.sp)
                            }
                            Text(text = "SPECTRAL TRACK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PurplePrimary, letterSpacing = 1.sp)
                        }

                        Text(text = selectedPath.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(text = selectedPath.description, fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Text(
                    text = "SYLLABUS MISSIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Lessons syllabus item list
            items(selectedPath.lessons) { lesson ->
                val isCompleted = profile.completedLessonIds.contains(lesson.id)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GhostCardBg, shape = RoundedCornerShape(18.dp))
                        .border(1.dp, GhostBorder, shape = RoundedCornerShape(18.dp))
                        .clickable { viewModel.selectLesson(lesson) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Check icon / state
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isCompleted) EmeraldAccent.copy(alpha = 0.15f) else GhostDarkBg,
                                shape = CircleShape
                            )
                            .border(1.dp, if (isCompleted) EmeraldAccent else GhostBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                        } else {
                            Text(text = "📖", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = lesson.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = lesson.summary, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Arrow
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = TextTertiary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// ==========================================
// 5. LESSON DETAILED LEARNING + AI SLIDE SHEET
// ==========================================
@Composable
fun LessonDetailsScreen(viewModel: GhostViewModel) {
    val lesson by viewModel.selectedLesson.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isChatOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Upper Head Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateTo(GhostScreen.PathDetail) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "ACTIVE SPECIFICATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 1.5.sp
                )
                IconButton(onClick = {
                    // Instantly trigger AI chatbot explanation slide
                    viewModel.requestAILessonExplanation(lesson.title, lesson.codeExample)
                    isChatOpen = true
                }) {
                    Text(text = "🤖", fontSize = 20.sp)
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = lesson.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(PurplePrimary.copy(alpha = 0.15f), shape = CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "⏳", fontSize = 10.sp)
                            Text(text = "${lesson.durationMin} MIN READ", color = PurplePrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(CyanAccent.copy(alpha = 0.15f), shape = CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+50 XP QUIZ", color = CyanAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Main explanation block
                Text(
                    text = lesson.detailedExplanation,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = TextSecondary
                )

                // Syntactical Code Block Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0E17), shape = RoundedCornerShape(16.dp))
                        .border(1.dp, GhostBorder, shape = RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(text = "CODENAME SNIPPET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextTertiary, modifier = Modifier.padding(bottom = 8.dp))
                    SelectionContainer {
                        Text(
                            text = lesson.codeExample,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyanAccent,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.requestAILessonExplanation(lesson.title, lesson.codeExample)
                            isChatOpen = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhostButtonBg),
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Ask Ghost AI", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = "✨", fontSize = 11.sp)
                        }
                    }
                }

                // Spook interactive Multiple Choice Quiz question
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GhostSubCardBg, shape = RoundedCornerShape(20.dp))
                        .border(1.dp, GhostBorder, shape = RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "KNOWLEDGE BARRIER", fontSize = 10.sp, color = OrangeAccent, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(text = lesson.quizQuestion, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lesson.quizOptions.forEachIndexed { index, option ->
                            val isSelectedAttempt = viewModel.quizAnswerAttempt == index
                            val correctIndex = lesson.quizAnswerIndex
                            val attemptBorder = if (isSelectedAttempt) {
                                if (index == correctIndex) EmeraldAccent else Color.Red
                            } else {
                                GhostBorder
                            }
                            val attemptBg = if (isSelectedAttempt) {
                                if (index == correctIndex) EmeraldAccent.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
                            } else {
                                GhostDarkBg
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(attemptBg, shape = RoundedCornerShape(12.dp))
                                    .border(1.dp, attemptBorder, shape = RoundedCornerShape(12.dp))
                                    .clickable { viewModel.submitQuizAnswer(index) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(2.dp, if (isSelectedAttempt) PurplePrimary else TextTertiary, CircleShape)
                                        .background(if (isSelectedAttempt) PurplePrimary else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (65 + index).toChar().toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedAttempt) Color.Black else TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = option, fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                    }

                    // Quiz Feedback Box
                    viewModel.quizFeedback?.let { feedback ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GhostDarkBg, shape = RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = feedback, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // SLIDING AI BOT CHAT OVERLAY
        if (isChatOpen) {
            GhostChatOverlay(viewModel, onClose = { isChatOpen = false })
        }
    }
}

// ==========================================
// 6. LEETCODE SANDBOX PRACTICE TERMINAL SCREEN
// ==========================================
@Composable
fun PracticeSandboxScreen(viewModel: GhostViewModel) {
    val challenge by viewModel.selectedChallenge.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
    ) {
        // Navigation Back-row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo(GhostScreen.Home) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "ALGORITHM PORTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiary,
                letterSpacing = 1.5.sp
            )
            IconButton(onClick = { viewModel.selectChallenge(challenge) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextPrimary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Description Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = challenge.emoji, fontSize = 24.sp)
                    Text(text = challenge.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                val levelColor = when (challenge.difficulty) {
                    "Easy" -> CyanAccent
                    "Medium" -> OrangeAccent
                    else -> PurplePrimary
                }

                Box(
                    modifier = Modifier
                        .background(levelColor.copy(alpha = 0.15f), shape = CircleShape)
                        .border(1.dp, levelColor.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(text = challenge.difficulty, color = levelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Challenge Description text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GhostSubCardBg, shape = RoundedCornerShape(18.dp))
                    .border(1.dp, GhostBorder, shape = RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "MISSION ORDER", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                    Text(
                        text = challenge.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Sample Input: ${challenge.testCaseInput}", fontSize = 11.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
                        Text(text = "Expected: ${challenge.testCaseExpectedOutput}", fontSize = 11.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Interactive Editor Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF07070C), shape = RoundedCornerShape(18.dp))
                    .border(1.dp, PurplePrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(18.dp))
            ) {
                // Editor header file representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0E17))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(Color.Red, CircleShape))
                        Box(modifier = Modifier.size(6.dp).background(Color.Yellow, CircleShape))
                        Box(modifier = Modifier.size(6.dp).background(Color.Green, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "spectral_solution.py", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }

                    Text(text = "UTF-8", fontSize = 10.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
                }

                // Code Input Area
                BasicTextField(
                    value = viewModel.codingBuffer,
                    onValueChange = { viewModel.codingBuffer = it },
                    textStyle = TextStyle(
                        color = CyanAccent,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    ),
                    cursorBrush = SolidColor(TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                        .testTag("code_editor_field")
                )
            }

            // Compiler controller action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val compiledSuccess = viewModel.terminalLogs.contains("[COMPILE SUCCESS]")

                Button(
                    onClick = { viewModel.runChallengeCode() },
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .testTag("compile_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (compiledSuccess) EmeraldAccent else PurplePrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.Black)
                        Text(
                            text = if (viewModel.isCompiling) "Accessing Compiler..." else "Compile & Run Tests",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Command Terminal Interface Output Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, shape = RoundedCornerShape(18.dp))
                    .border(1.dp, GhostBorder, shape = RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Text(text = "SHELL OUTPUT LOGGER", fontSize = 9.sp, color = TextTertiary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))

                Text(
                    text = viewModel.terminalLogs,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (viewModel.terminalLogs.contains("ERROR")) Color(0xFFF43F5E) else TextPrimary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==========================================
// 7. EXPANDED SEARCH & EXPLORE CONSOLE
// ==========================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: GhostViewModel) {
    val results = viewModel.performSearch()
    val categories = listOf("All", "Python", "Kotlin", "JS", "Easy", "Hard")
    val profile by viewModel.profileState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "SEARCH & EXPLORE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Rounded Dynamic Search Input
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            placeholder = { Text("Search lessons, loops, schemas...", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextTertiary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input_txt"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = GhostBorder,
                focusedContainerColor = GhostCardBg,
                unfocusedContainerColor = GhostCardBg,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextSecondary
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Categorized Filtering Grid Badges
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isActive = viewModel.searchFilterByCategory == cat
                Box(
                    modifier = Modifier
                        .background(
                            if (isActive) PurplePrimary else GhostSubCardBg,
                            shape = CircleShape
                        )
                        .border(1.dp, if (isActive) PurplePrimary else GhostBorder, CircleShape)
                        .clickable { viewModel.searchFilterByCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isActive) Color.Black else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Searched Results Container
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Match challenge section
            val finalFilteredChallenges = results.challenges.filter { ch ->
                val category = viewModel.searchFilterByCategory
                when (category) {
                    "All" -> true
                    "Python" -> ch.category.lowercase().contains("loop") || ch.category.lowercase().contains("array")
                    "Kotlin" -> ch.category.lowercase().contains("null")
                    "JS" -> ch.category.lowercase().contains("sec")
                    "Easy" -> ch.difficulty == "Easy"
                    "Hard" -> ch.difficulty == "Hard"
                    else -> true
                }
            }

            if (finalFilteredChallenges.isNotEmpty()) {
                item {
                    Text(text = "CHALLENGES MATCHED (${finalFilteredChallenges.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp)
                }

                items(finalFilteredChallenges) { ch ->
                    val isSolved = profile.solvedChallengeIds.contains(ch.id)
                    ChallengeCard(challenge = ch, isSolved = isSolved) {
                        viewModel.selectChallenge(ch)
                    }
                }
            }

            // Match lessons section
            val finalFilteredLessons = results.lessons.filter { lessonPair ->
                val category = viewModel.searchFilterByCategory
                when (category) {
                    "All" -> true
                    "Python" -> lessonPair.first.id.contains("python")
                    "Kotlin" -> lessonPair.first.id.contains("kotlin")
                    "JS" -> lessonPair.first.id.contains("js")
                    "Easy" -> lessonPair.second.durationMin <= 7
                    "Hard" -> lessonPair.second.durationMin > 7
                    else -> true
                }
            }

            if (finalFilteredLessons.isNotEmpty()) {
                item {
                    Text(text = "LESSONS MATCHED (${finalFilteredLessons.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
                }

                items(finalFilteredLessons) { lessonPair ->
                    val isCompleted = profile.completedLessonIds.contains(lessonPair.second.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GhostSubCardBg, shape = RoundedCornerShape(18.dp))
                            .border(1.dp, GhostBorder, shape = RoundedCornerShape(18.dp))
                            .clickable { viewModel.selectLesson(lessonPair.second) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (isCompleted) "✅" else "📖", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = lessonPair.second.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = lessonPair.first.title, fontSize = 11.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Arrow", tint = TextTertiary)
                    }
                }
            }

            if (finalFilteredChallenges.isEmpty() && finalFilteredLessons.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "📭", fontSize = 48.sp)
                            Text(text = "No results match search specs.", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Try searching variables, loop, spectral, or easy.", color = TextTertiary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. NOTIFICATIONS SCREEN
// ==========================================
@Composable
fun NotificationsScreen(viewModel: GhostViewModel) {
    val notifications by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo(GhostScreen.Home) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "COMMUNICATION LOGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiary,
                letterSpacing = 1.5.sp
            )
            IconButton(onClick = { viewModel.markAllAsRead() }) {
                Icon(Icons.Default.Check, contentDescription = "Mark All Read", tint = TextPrimary)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "😴", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "All communications clear.", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Finish python quizzes to trigger rank badges.", color = TextTertiary, fontSize = 11.sp)
                        }
                    }
                }
            }

            items(notifications) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (item.isRead) GhostSubCardBg else Color(0xFF131124), shape = RoundedCornerShape(18.dp))
                        .border(1.dp, if (item.isRead) GhostBorder else PurplePrimary.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    val badgeColor = when (item.type) {
                        "streak" -> OrangeAccent
                        "reward" -> EmeraldAccent
                        else -> PurplePrimary
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(badgeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (item.type) {
                                "streak" -> "🔥"
                                "reward" -> "🎉"
                                else -> "👻"
                            },
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = item.timeAgo, fontSize = 9.sp, color = TextTertiary)
                        }
                        Text(text = item.message, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.dismissNotification(item.id) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Text(text = "×", color = TextTertiary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. PROFILE & EXPANDED CUSTOM SETTINGS PAGE
// ==========================================
@Composable
fun ProfileScreen(viewModel: GhostViewModel) {
    val profile by viewModel.profileState.collectAsState()
    val scrollState = rememberScrollState()
    var isChatBoxVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
    ) {
        // High Contrast Profile header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo(GhostScreen.Home) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "GHOST DECRYPTOR PROFILE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiary,
                letterSpacing = 1.5.sp
            )
            IconButton(onClick = { viewModel.navigateTo(GhostScreen.Settings) }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Main avatar card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GhostCardBg, GhostSubCardBg)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .border(1.dp, GhostBorder, shape = RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.linearGradient(colors = listOf(PurplePrimary, IndigoPrimary)),
                                shape = CircleShape
                            )
                            .border(3.dp, PurplePrimary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👻", fontSize = 42.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = profile.username, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = profile.email, fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    BasicTextField(
                        value = profile.bio,
                        onValueChange = { viewModel.editBio(it) },
                        textStyle = TextStyle(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .background(GhostDarkBg, shape = RoundedCornerShape(12.dp))
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }

            // XP Rank statistics detail grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Completed Lessons Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(GhostSubCardBg, shape = RoundedCornerShape(18.dp))
                        .border(1.dp, GhostBorder, shape = RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(text = "📖 LESSONS", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                        Text(text = "${profile.completedLessonIds.size} Complete", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                // Solved Leetcodes Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(GhostSubCardBg, shape = RoundedCornerShape(18.dp))
                        .border(1.dp, GhostBorder, shape = RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(text = "🛡️ MISSIONS", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        Text(text = "${profile.solvedChallengeIds.size} Deployed", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Floating Custom Chatbot sandbox trigger button
            Button(
                onClick = { isChatBoxVisible = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, PurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "💬 Ask Live Ghost AI Tutor", fontSize = 13.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                }
            }

            if (isChatBoxVisible) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .border(1.dp, GhostBorder, RoundedCornerShape(20.dp)),
                    color = GhostSubCardBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box {
                        GhostChatOverlay(viewModel, onClose = { isChatBoxVisible = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==========================================
// 10. CODESPACES GENERAL SETTINGS PAGE
// ==========================================
@Composable
fun SettingsScreen(viewModel: GhostViewModel) {
    val profile by viewModel.profileState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostDarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo(GhostScreen.Profile) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "COGNITIVE PREFERENCES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiary,
                letterSpacing = 1.5.sp
            )
            IconButton(onClick = { }, enabled = false) {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GhostSubCardBg, shape = RoundedCornerShape(22.dp))
                    .border(1.dp, GhostBorder, shape = RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "PREFERENCES", fontSize = 11.sp, color = PurplePrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                    // Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Daily reminders", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Acquire notifications of streak resets.", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = profile.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications() },
                            colors = SwitchDefaults.colors(checkedThumbColor = PurplePrimary)
                        )
                    }

                    // Tonal sound toggler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Spook Synthesizer loops", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Audio click elements inside terminal.", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = profile.isSoundEffectsEnabled,
                            onCheckedChange = { viewModel.toggleSoundEffects() },
                            colors = SwitchDefaults.colors(checkedThumbColor = PurplePrimary)
                        )
                    }

                    // Dark Mode Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Immersive OLED Dark", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Locked securely in dark mode to preserve eyes.", fontSize = 11.sp, color = TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .background(PurplePrimary.copy(alpha = 0.15f), shape = CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "ACTIVE", color = PurplePrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Gemini API Setup explanation help card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GhostSubCardBg, shape = RoundedCornerShape(22.dp))
                    .border(1.dp, GhostBorder, shape = RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "GEMINI TUTOR KEY CONFIG", fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                    Text(
                        text = "To unlock dynamic interactive code assistance, register your GEMINI_API_KEY inside the secure Secrets panel on Google AI Studio. This gives you instant unlimited access to Google's super-compiler tutor.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (GeminiHelper.isKeyConfigured()) EmeraldAccent else Color.Red,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (GeminiHelper.isKeyConfigured()) "Live model online (gemini-3.5-flash)" else "Offline mock backup active",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Log out block
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Egress Terminal (Log Out)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==========================================
// FLOATING SLIDING GHOST CHAT DIALOG OVERLAY
// ==========================================
@Composable
fun GhostChatOverlay(
    viewModel: GhostViewModel,
    onClose: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by remember { derivedStateOf { viewModel.isChatLoading } }
    val keyboardScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(top = 16.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(PurplePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👻", fontSize = 14.sp)
                }
                Column {
                    Text(text = "Ghost AI Assistant", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (GeminiHelper.isKeyConfigured()) "Powered by Gemini 3.5" else "Running in backup mode",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(onClick = onClose) {
                Text(text = "Dismiss", color = PurplePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Horizontal line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GhostBorder)
                .padding(vertical = 4.dp)
        )

        // Messages Box
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isUser) PurplePrimary else GhostSubCardBg,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                            .border(1.dp, if (isUser) PurplePrimary else GhostBorder, shape = RoundedCornerShape(16.dp))
                            .padding(12.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isUser) Color.Black else TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(GhostSubCardBg, shape = RoundedCornerShape(14.dp))
                                .border(1.dp, GhostBorder, shape = RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💭 Casting explanation spell...",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
            }
        }

        // Input Field Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GhostSubCardBg,
            border = borderStroke(1.dp, GhostBorder)
        ) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = viewModel.chatInput,
                    onValueChange = { viewModel.chatInput = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(TextPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .background(GhostDarkBg, shape = RoundedCornerShape(12.dp))
                        .padding(10.dp)
                        .testTag("chat_input_text_field"),
                    decorationBox = { innerTextField ->
                        if (viewModel.chatInput.isEmpty()) {
                            Text(text = "Ask ghost to review code...", color = TextTertiary, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { viewModel.sendChatMessage() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(PurplePrimary, RoundedCornerShape(10.dp))
                        .testTag("send_chat_msg_btn")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// Helper border stroke builder to avoid generic material dependency details
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = BorderStroke(width, color)

// ==========================================
// SEAMLESS INTEGRATIVE BOTTOM NAV BAR WIDGET
// ==========================================
@Composable
fun GhostBottomNavigation(
    activeScreen: GhostScreen,
    onTabSelected: (GhostScreen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GhostBorder, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)),
        color = GhostSubCardBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabItem(
                label = "Home",
                emoji = "🏠",
                isActive = activeScreen == GhostScreen.Home,
                onClick = { onTabSelected(GhostScreen.Home) }
            )

            BottomTabItem(
                label = "Path",
                emoji = "📖",
                isActive = activeScreen == GhostScreen.PathDetail || activeScreen == GhostScreen.LessonDetail,
                onClick = { onTabSelected(GhostScreen.PathDetail) }
            )

            BottomTabItem(
                label = "Search",
                emoji = "🔍",
                isActive = activeScreen == GhostScreen.Search,
                onClick = { onTabSelected(GhostScreen.Search) }
            )

            BottomTabItem(
                label = "Level",
                emoji = "🏆",
                isActive = activeScreen == GhostScreen.Practice,
                onClick = { onTabSelected(GhostScreen.Practice) }
            )

            BottomTabItem(
                label = "Setup",
                emoji = "⚙️",
                isActive = activeScreen == GhostScreen.Profile || activeScreen == GhostScreen.Settings,
                onClick = { onTabSelected(GhostScreen.Profile) }
            )
        }
    }
}

@Composable
fun BottomTabItem(
    label: String,
    emoji: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .width(60.dp)
    ) {
        val sizeAnim by animateDpAsState(
            targetValue = if (isActive) 14.dp else 0.dp,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
            label = "tab_scale"
        )

        Box(
            modifier = Modifier
                .width(48.dp)
                .height(28.dp)
                .background(
                    if (isActive) PurplePrimary.copy(alpha = 0.15f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isActive) 1.dp else 0.dp,
                    color = if (isActive) PurplePrimary.copy(alpha = 0.25f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 16.sp)
        }

        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isActive) TextPrimary else TextTertiary,
            fontWeight = FontWeight.Bold
        )
    }
}
