import os
import re

fixes = {
    "app/src/main/java/com/sanket_satpute_20/ironmind/stats/StatsScreen.kt": "StatsScreen()",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/language/LanguageSelectionScreen.kt": "LanguageSelectionScreen(onLanguageSelected = {})",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/splash/SplashScreen.kt": "SplashScreen(onSplashComplete = {})",
    "app/src/main/java/com/sanket_satpute_20/ironmind/voice/VoiceCheckInScreen.kt": "VoiceCheckInScreen(taskName = \"Mission\", onComplete = {})",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/components/EnergyCoreUI.kt": "EnergyCoreUI(progress = 0.5f, modifier = androidx.compose.ui.Modifier, remainingLabel = \"50%\", accent = androidx.compose.ui.graphics.Color.Cyan, phaseLabel = \"Phase 1\")",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/components/IntegrityCoreVisual.kt": "IntegrityCoreVisual(streak = 5, shields = 2, isCritical = false)",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/components/IronMindLexiconSheet.kt": "IronMindLexiconSheet(term = null, onDismiss = {})",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/components/RotaryDualRingPicker.kt": "RotaryDualRingPicker(primaryValue = 5, secondaryValue = 30, primaryMaxValue = 12, secondaryMaxValue = 60, primaryStepDegrees = 30f, secondaryStepDegrees = 6f, primaryMarkerCount = 12, secondaryMarkerCount = 60, primaryAccent = androidx.compose.ui.graphics.Color.Blue, secondaryAccent = androidx.compose.ui.graphics.Color.Red, onPrimaryValueChange = {}, onSecondaryValueChange = {}, centerContent = { _, _, _ -> })"
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

for f, call in fixes.items():
    try:
        name = os.path.basename(f).replace('.kt', '')
        with open(f, 'r', encoding='utf-8') as file:
            content = file.read()
        
        # Remove ALL preview blocks (including those from phase 5 if any, to be clean)
        pattern = r'@androidx\.compose\.ui\.tooling\.preview\.Preview\(showBackground = true\)\s*@androidx\.compose\.runtime\.Composable\s*fun \w+Preview\(\) \{[\s\S]*?\n\}'
        
        content = re.sub(pattern, '', content)
        
        content += preview_template.format(name=name, call=call)
        
        with open(f, 'w', encoding='utf-8') as file:
            file.write(content)
        print(f"Fixed {name}")
    except Exception as e:
        print(f"Error {name}: {e}")

