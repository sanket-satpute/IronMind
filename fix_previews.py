import os

fixes = {
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/language/LanguageSelectionScreen.kt": "LanguageSelectionScreen()",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/splash/SplashScreen.kt": "SplashScreen()",
    "app/src/main/java/com/sanket_satpute_20/ironmind/utils/PermissionCenterScreen.kt": "PermissionCenterScreen()",
    "app/src/main/java/com/sanket_satpute_20/ironmind/utils/PermissionRationaleScreen.kt": "PermissionRationaleScreen(permissionTypeStr = \"Accessibility\", onBack = {}, onContinueToSettings = {})",
    "app/src/main/java/com/sanket_satpute_20/ironmind/voice/VoiceCheckInScreen.kt": "VoiceCheckInScreen(taskName = \"Mission\")",
    "app/src/main/java/com/sanket_satpute_20/ironmind/voice/VoiceLogsScreen.kt": "VoiceLogsScreen()",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/components/OrbitalTimePicker.kt": "OrbitalTimePicker(initialTime = java.time.LocalTime.now(), onTimeChange = {})",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/components/LiveIntegrityShieldCard.kt": "LiveIntegrityShieldCard(state = com.sanket_satpute_20.ironmind.integrity.ShieldUiState(), onEventHandled = {})",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/components/ParticleSystem.kt": "FloatingXP(onAnimationEnd = {})",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/components/RotaryDualRingPicker.kt": "RotaryDualRingPicker(primaryValue = 5, secondaryValue = 30, primaryMaxValue = 12, secondaryMaxValue = 60, primaryStepDegrees = 30f, secondaryStepDegrees = 6f, onPrimaryValueChange = {}, onSecondaryValueChange = {}, primaryMarkerCount = 12, secondaryMarkerCount = 60, primaryAccent = androidx.compose.ui.graphics.Color.Blue, secondaryAccent = androidx.compose.ui.graphics.Color.Red, centerContent = {})"
}

preview_template = \"\"\"
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun {name}Preview() {{
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {{
        {call}
    }}
}}
\"\"\"

import re

for f, call in fixes.items():
    try:
        name = os.path.basename(f).replace('.kt', '')
        # Remove any existing previews
        with open(f, 'r', encoding='utf-8') as file:
            content = file.read()
        pattern = r'@androidx\.compose\.ui\.tooling\.preview\.Preview\(showBackground = true\)\s*@androidx\.compose\.runtime\.Composable\s*fun \w+Preview\(\) \{[\s\S]*?\}\s*\}'
        content = re.sub(pattern, '', content)
        
        # append
        content += preview_template.format(name=name, call=call)
        
        with open(f, 'w', encoding='utf-8') as file:
            file.write(content)
        print(f"Fixed {name}")
    except Exception as e:
        print(f"Error {name}: {e}")

