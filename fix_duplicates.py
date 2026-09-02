import re

files_to_fix = [
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/language/LanguageSelectionScreen.kt",
    "app/src/main/java/com/sanket_satpute_20/ironmind/ui/splash/SplashScreen.kt",
    "app/src/main/java/com/sanket_satpute_20/ironmind/utils/PermissionCenterScreen.kt",
    "app/src/main/java/com/sanket_satpute_20/ironmind/utils/PermissionRationaleScreen.kt",
    "app/src/main/java/com/sanket_satpute_20/ironmind/voice/VoiceCheckInScreen.kt",
    "app/src/main/java/com/sanket_satpute_20/ironmind/voice/VoiceLogsScreen.kt"
]

pattern = r'@androidx\.compose\.ui\.tooling\.preview\.Preview\(showBackground = true\)\s*@androidx\.compose\.runtime\.Composable\s*fun \w+Preview\(\) \{[\s\S]*?\}\s*\}'

for f in files_to_fix:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Replace all matches with empty string
    content_no_previews = re.sub(pattern, '', content)
    
    with open(f, 'w', encoding='utf-8') as file:
        file.write(content_no_previews)

