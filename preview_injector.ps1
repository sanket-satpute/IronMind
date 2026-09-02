using namespace System.Text.RegularExpressions

$files = @(
    "app\src\main\java\com\sanket_satpute_20\ironmind\home\HomeScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\home\MainScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\home\ProfileScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\home\ProgressScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\home\GamifiedHeader.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\home\HomeTaskCards.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\home\HomeTopSections.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentitySetupScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentityFlashOverlay.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\identity\IdentityLevelUpOverlay.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\integrity\NeutralBlockActivity.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\morninglaunch\MorningLaunchActivity.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\morninglaunch\MorningDayBoardActivity.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\morninglaunch\MorningLaunchMonkFlow.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\morninglaunch\MorningUnlockReviewActivity.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\navigation\MainNavHost.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\navigation\OnboardingRoot.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\AppSelectorScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\IntensityScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\IntroPromiseScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\TaskBuilderScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\onboarding\WebsiteSelectorScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\operations\OperationsScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\psychology\DiagnosticScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\psychology\HonestyOverridePanel.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\psychology\ModeSwitcherScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\psychology\ProfileRevealScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\psychology\WeeklyCheckInScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\punishment\PunishmentActivity.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\rewards\DailyCompleteScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\rewards\StreakCelebrationScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\settings\PsychologySettingsScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\settings\PrivacyAndDataScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\settings\ManualTypeSelectScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\share\ShareableCardScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\sleeplock\SleepLockActivity.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\sleeplock\SleepLockSetupScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\social\ContactDiscoveryScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\social\FriendCompareScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\social\IronCircleScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\crucible\CrucibleCompleteScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\crucible\CrucibleFailedScreen.kt",
    "app\src\main\java\com\sanket_satpute_20\ironmind\crucible\CrucibleScreen.kt"
)

$previewTemplate = @"

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun {0}Preview() {{
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {{
        {0}()
    }}
}}
"@

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        
        # Determine the name of the main composable based on filename
        $name = [System.IO.Path]::GetFileNameWithoutExtension($file)
        
        # If it doesn't already have a preview for this file
        if ($content -notmatch "fun $($name)Preview") {
            # Find the signature of the main composable
            $pattern = "fun $name\((.*?)\)"
            $match = [Regex]::Match($content, $pattern, [System.Text.RegularExpressions.RegexOptions]::Singleline)
            
            if ($match.Success) {
                # We won't automatically parse complex parameters in this pass. 
                # We will output which ones need manual mocking.
                $params = $match.Groups[1].Value
                if ($params.Trim() -eq "") {
                    $content += ($previewTemplate -f $name)
                    Set-Content -Path $file -Value $content -NoNewline
                    Write-Host "Added basic preview to $name"
                } else {
                    Write-Host "Skipping $name - has parameters: $params"
                }
            } else {
                Write-Host "Could not find signature for $name"
            }
        } else {
            Write-Host "$name already has a preview."
        }
    }
}
