$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\home\GamifiedHeader.kt -Raw
$content = $content -replace "currentMode = com.sanket_satpute_20.ironmind.psychology.ModeState.MONK", "currentMode = com.sanket_satpute_20.ironmind.psychology.AppMode.MONK"
$content = $content -replace "onMilestoneClick: \(\) -> Unit,`r?`nonProfileClick: \(\) -> Unit,", ""
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\home\GamifiedHeader.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentitySetupScreen.kt -Raw
$content = $content -replace "fun IdentitySetupScreen\(onNext: \(String, List<String>\) -> Unit, onBack: \(\) -> Unit\)", "fun IdentitySetupScreen(onNext: () -> Unit = {}, onBack: () -> Unit = {})"
$content = $content -replace "IdentitySetupScreen\(onNext = {}\)", "IdentitySetupScreen(onNext = {}, onBack = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentitySetupScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\IntensityScreen.kt -Raw
$content = $content -replace "fun IntensityScreen\(onNext: \(String\) -> Unit, onBack: \(\) -> Unit\)", "fun IntensityScreen(onNext: () -> Unit = {}, onBack: () -> Unit = {})"
$content = $content -replace "IntensityScreen\(onNext = {}\)", "IntensityScreen(onNext = {}, onBack = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\IntensityScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\TaskBuilderScreen.kt -Raw
$content = $content -replace "fun TaskBuilderScreen\(\s*onNext: \(List<com.sanket_satpute_20.ironmind.data.Task>\) -> Unit,\s*onBack: \(\) -> Unit,", "fun TaskBuilderScreen(onNext: () -> Unit = {}, onBack: () -> Unit = {},"
$content = $content -replace "TaskBuilderScreen\(onNext = {}\)", "TaskBuilderScreen(onNext = {}, onBack = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\TaskBuilderScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\WebsiteSelectorScreen.kt -Raw
$content = $content -replace "fun WebsiteSelectorScreen\(onBack: \(\) -> Unit, onNext: \(List<String>\) -> Unit\)", "fun WebsiteSelectorScreen(onBack: () -> Unit = {}, onNext: () -> Unit = {})"
$content = $content -replace "WebsiteSelectorScreen\(onBack = {}\)", "WebsiteSelectorScreen(onBack = {}, onNext = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\WebsiteSelectorScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\psychology\ModeSwitcherScreen.kt -Raw
$content = $content -replace "fun ModeSwitcherScreen\(onSaved: \(\) -> Unit, onCancel: \(\) -> Unit\)", "fun ModeSwitcherScreen(onSaved: () -> Unit = {}, onCancel: () -> Unit = {})"
$content = $content -replace "ModeSwitcherScreen\(onSaved = {}\)", "ModeSwitcherScreen(onSaved = {}, onCancel = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\psychology\ModeSwitcherScreen.kt $content -NoNewline

$content = Get-Content app\src\main\java\com\sanket_satpute_20\ironmind\settings\ManualTypeSelectScreen.kt -Raw
$content = $content -replace "fun ManualTypeSelectScreen\(onSaved: \(\) -> Unit, onBack: \(\) -> Unit\)", "fun ManualTypeSelectScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {})"
$content = $content -replace "ManualTypeSelectScreen\(onSaved = {}\)", "ManualTypeSelectScreen(onSaved = {}, onBack = {})"
Set-Content app\src\main\java\com\sanket_satpute_20\ironmind\settings\ManualTypeSelectScreen.kt $content -NoNewline

Write-Host "Done"
