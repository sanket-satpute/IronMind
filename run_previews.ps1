# 1. GamifiedHeader.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\home\GamifiedHeader.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun GamifiedHeaderPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        GamifiedHeader(
            level = 5,
            progress = 0.7f,
            streak = 12,
            currentMode = com.sanket_satpute_20.ironmind.psychology.AppMode.MONK,
            onGraveyardClick = {},
            onMilestoneClick = {},
            onProfileClick = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\home\GamifiedHeader.kt -Value $content -NoNewline
Write-Host "Added to GamifiedHeader"

# 2. IdentitySetupScreen.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentitySetupScreen.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun IdentitySetupScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        IdentitySetupScreen(
            onNext = { _, _ -> },
            onBack = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentitySetupScreen.kt -Value $content -NoNewline
Write-Host "Added to IdentitySetupScreen"

# 3. IntensityScreen.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\IntensityScreen.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun IntensityScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        IntensityScreen(
            onNext = {},
            onBack = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\IntensityScreen.kt -Value $content -NoNewline
Write-Host "Added to IntensityScreen"

# 4. TaskBuilderScreen.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\TaskBuilderScreen.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun TaskBuilderScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        if (androidx.compose.ui.platform.LocalInspectionMode.current) return@IronMindTheme
        TaskBuilderScreen(
            onNext = {},
            onBack = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\TaskBuilderScreen.kt -Value $content -NoNewline
Write-Host "Added to TaskBuilderScreen"

# 5. WebsiteSelectorScreen.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\WebsiteSelectorScreen.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun WebsiteSelectorScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        WebsiteSelectorScreen(
            onBack = {},
            onNext = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\WebsiteSelectorScreen.kt -Value $content -NoNewline
Write-Host "Added to WebsiteSelectorScreen"

# 6. HonestyOverridePanel.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\psychology\HonestyOverridePanel.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun HonestyOverridePanelPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HonestyOverridePanel(
            lockType = "Doomscrolling",
            onOverride = {},
            onCancel = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\psychology\HonestyOverridePanel.kt -Value $content -NoNewline
Write-Host "Added to HonestyOverridePanel"

# 7. ModeSwitcherScreen.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\psychology\ModeSwitcherScreen.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ModeSwitcherScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ModeSwitcherScreen(
            onSaved = {},
            onCancel = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\psychology\ModeSwitcherScreen.kt -Value $content -NoNewline
Write-Host "Added to ModeSwitcherScreen"

# 8. PrivacyAndDataScreen.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\settings\PrivacyAndDataScreen.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun PrivacyAndDataScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PrivacyAndDataScreen(
            onBack = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\settings\PrivacyAndDataScreen.kt -Value $content -NoNewline
Write-Host "Added to PrivacyAndDataScreen"

# 9. ManualTypeSelectScreen.kt
$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\settings\ManualTypeSelectScreen.kt -Raw
$content += @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ManualTypeSelectScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ManualTypeSelectScreen(
            onSaved = {},
            onBack = {}
        )
    }
}
"@
Set-Content -Path app\src\main\java\com\sanket_satpute_20\ironmind\settings\ManualTypeSelectScreen.kt -Value $content -NoNewline
Write-Host "Added to ManualTypeSelectScreen"

