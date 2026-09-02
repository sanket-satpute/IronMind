package com.sanket_satpute_20.ironmind.ui.language

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

// ── Data model ────────────────────────────────────────────────
data class AppLanguage(
    val code        : String,   // BCP-47 language tag
    val flag        : String,   // emoji flag
    val englishName : String,   // "Hindi"
    val nativeName  : String,   // "हिन्दी"
    val greeting    : String    // shown when selected
)

val SUPPORTED_LANGUAGES = listOf(
    AppLanguage("en",    "🇬🇧", "English",    "English",       "Let's build discipline."),
    AppLanguage("hi",    "🇮🇳", "Hindi",      "हिन्दी",        "अनुशासन बनाते हैं।"),
    AppLanguage("mr",    "🇮🇳", "Marathi",    "मराठी",         "शिस्त लावूया।"),
    AppLanguage("ta",    "🇮🇳", "Tamil",      "தமிழ்",         "ஒழுக்கம் வளர்ப்போம்."),
    AppLanguage("te",    "🇮🇳", "Telugu",     "తెలుగు",        "క్రమశిక్షణ నేర్చుకుందాం."),
    AppLanguage("kn",    "🇮🇳", "Kannada",    "ಕನ್ನಡ",        "ಶಿಸ್ತು ಕಲಿಯೋಣ."),
    AppLanguage("gu",    "🇮🇳", "Gujarati",   "ગુજરાતી",       "શિસ્ત બનાવીએ."),
    AppLanguage("bn",    "🇧🇩", "Bengali",    "বাংলা",         "শৃঙ্খলা গড়ি।"),
    AppLanguage("pa",    "🇮🇳", "Punjabi",    "ਪੰਜਾਬੀ",        "ਅਨੁਸ਼ਾਸਨ ਬਣਾਈਏ।"),
    AppLanguage("ur",    "🇵🇰", "Urdu",       "اردو",          ".آؤ نظم و ضبط سیکھیں"),
    AppLanguage("es",    "🇪🇸", "Spanish",    "Español",       "Construyamos disciplina."),
    AppLanguage("ar",    "🇸🇦", "Arabic",     "العربية",       ".لنبنِ الانضباط")
)

// ── Screen ────────────────────────────────────────────────────
@Composable
fun LanguageSelectionScreen(onLanguageSelected: (AppLanguage) -> Unit) {

    val context = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    var selectedCode  by remember { mutableStateOf<String?>(null) }
    var headerVisible by remember { mutableStateOf(false) }

    // Staggered entrance
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        headerVisible = true
    }

    val headerAlpha by animateFloatAsState(
        targetValue  = if (headerVisible) 1f else 0f,
        animationSpec = tween(700)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(56.dp))

            // ── Header ───────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(headerAlpha)
                    .padding(horizontal = 28.dp)
            ) {
                Text("🌐", fontSize = 44.sp)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text       = "Choose Your Language",
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Black,
                    color      = Color.White,
                    textAlign  = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text      = "IronMind speaks your language.\nSelect the one you think in.",
                    fontSize  = 14.sp,
                    color     = SurfaceElevated,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Language grid ─────────────────────────────
            LazyVerticalGrid(
                columns             = GridCells.Fixed(2),
                contentPadding      = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                // Grid inside scroll — fixed height trick
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        // 3 rows × card height + gaps
                        ((SUPPORTED_LANGUAGES.size / 2 + 1) * 100 +                         (SUPPORTED_LANGUAGES.size / 2) * 12).dp
                    )
            ) {
                items(SUPPORTED_LANGUAGES) { language ->
                    LanguageCard(
                        language   = language,
                        isSelected = selectedCode == language.code,
                        onClick    = { selectedCode = language.code }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Greeting preview ──────────────────────────
            AnimatedVisibility(
                visible = selectedCode != null,
                enter   = fadeIn(tween(400)) + slideInVertically { it / 2 },
                exit    = fadeOut(tween(200))
            ) {
                val selected = SUPPORTED_LANGUAGES.find { it.code == selectedCode }
                if (selected != null) {
                    Surface(
                        color  = DeepBackground,
                        shape  = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text      = selected.flag,
                                fontSize  = 32.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text      = selected.greeting,
                                fontSize  = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color     = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text     = selected.englishName,
                                fontSize = 13.sp,
                                color    = SurfaceElevated
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Confirm button ────────────────────────────
            Button(
                onClick  = {
                    val lang = SUPPORTED_LANGUAGES.find { it.code == selectedCode }
                    if (lang != null) {
                        prefs.appLanguage = lang.code
                        prefs.languageSelected = true
                        onLanguageSelected(lang)
                    }
                },
                enabled  = selectedCode != null,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCode != null)
                        ErrorRed else SurfaceDark,
                    disabledContainerColor = SurfaceDark
                ),
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(58.dp)
            ) {
                Text(
                    text       = if (selectedCode != null)
                        "CONTINUE IN ${
                            SUPPORTED_LANGUAGES.find { it.code == selectedCode }
                                ?.englishName?.uppercase() ?: ""
                        } →"
                    else
                        "Select a language to continue",
                    fontWeight = FontWeight.Black,
                    fontSize   = 14.sp,
                    color      = if (selectedCode != null) Color.White else SurfaceElevated
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── Language card ─────────────────────────────────────────────
@Composable
fun LanguageCard(
    language   : AppLanguage,
    isSelected : Boolean,
    onClick    : () -> Unit
) {
    val borderColor = if (isSelected) ErrorRed else SurfaceDark
    val bgColor     = if (isSelected) DeepBackground else DeepBackground
    val scale by animateFloatAsState(
        targetValue  = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Surface(
        color    = bgColor,
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 8.dp)
        ) {
            Text(text = language.flag, fontSize = 28.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = language.nativeName,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = if (isSelected) Color.White else TextPrimary,
                textAlign  = TextAlign.Center,
                maxLines   = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text      = language.englishName,
                fontSize  = 11.sp,
                color     = if (isSelected) WarningAmber else SurfaceElevated,
                textAlign = TextAlign.Center
            )

            // Selected checkmark
            if (isSelected) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "✓", fontSize = 14.sp, color = ErrorRed,
                    fontWeight = FontWeight.Black)
            }
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun LanguageSelectionScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        LanguageSelectionScreen(onLanguageSelected = {})
    }
}