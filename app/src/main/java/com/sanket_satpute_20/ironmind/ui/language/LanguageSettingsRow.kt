package com.sanket_satpute_20.ironmind.ui.language

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground

@Composable
fun LanguageSettingsRow(onChangeTapped: () -> Unit) {
    val context = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    val currentCode = prefs.appLanguage
    val currentLang = SUPPORTED_LANGUAGES.find { it.code == currentCode }

    Surface(
        color    = DeepBackground,
        shape    = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onChangeTapped)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currentLang?.flag ?: "🌐", fontSize = 26.sp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Language",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
                Text(
                    "${currentLang?.nativeName} • ${currentLang?.englishName}",
                    fontSize = 12.sp,
                    color    = Color.Gray
                )
            }

            Text("›", fontSize = 22.sp, color = Color.Gray)
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun LanguageSettingsRowPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        LanguageSettingsRow(onChangeTapped = {})
    }
}
