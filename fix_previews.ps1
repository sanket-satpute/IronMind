$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\home\GamifiedHeader.kt -Raw
$content = $content -replace "(?s)GamifiedHeader\([^)]*\)", "GamifiedHeader(level = 5, progress = 0.7f, streak = 12, currentMode = com.sanket_satpute_20.ironmind.psychology.ModeState.MONK, onGraveyardClick = {}, onSettingsClick = {}, onStatsClick = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\home\GamifiedHeader.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentitySetupScreen.kt -Raw
$content = $content -replace "(?s)IdentitySetupScreen\([^)]*\)", "IdentitySetupScreen(onNext = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentitySetupScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\IntensityScreen.kt -Raw
$content = $content -replace "(?s)IntensityScreen\([^)]*\)", "IntensityScreen(onNext = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\IntensityScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\TaskBuilderScreen.kt -Raw
$content = $content -replace "(?s)TaskBuilderScreen\([^)]*\)", "TaskBuilderScreen(onNext = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\TaskBuilderScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\WebsiteSelectorScreen.kt -Raw
$content = $content -replace "(?s)WebsiteSelectorScreen\([^)]*\)", "WebsiteSelectorScreen(onBack = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\WebsiteSelectorScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\psychology\HonestyOverridePanel.kt -Raw
$content = $content -replace "(?s)HonestyOverridePanel\([^)]*\)", "HonestyOverridePanel(lockType = `"Doomscrolling`", onOverride = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\psychology\HonestyOverridePanel.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\psychology\ModeSwitcherScreen.kt -Raw
$content = $content -replace "(?s)ModeSwitcherScreen\([^)]*\)", "ModeSwitcherScreen(onSaved = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\psychology\ModeSwitcherScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\settings\ManualTypeSelectScreen.kt -Raw
$content = $content -replace "(?s)ManualTypeSelectScreen\([^)]*\)", "ManualTypeSelectScreen(onSaved = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\settings\ManualTypeSelectScreen.kt $content -NoNewline

Write-Host "Replaced!"
