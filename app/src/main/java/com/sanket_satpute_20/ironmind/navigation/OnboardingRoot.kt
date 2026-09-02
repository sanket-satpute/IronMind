package com.sanket_satpute_20.ironmind.navigation

import androidx.compose.runtime.*
import com.sanket_satpute_20.ironmind.onboarding.IntensityScreen

enum class OnboardingStep {
    INTENSITY
}

data class OnboardingResult(
    val intensity: String
)

@Composable
fun OnboardingRoot(onOnboardingComplete: (OnboardingResult) -> Unit) {
    var currentStep by remember { mutableStateOf(OnboardingStep.INTENSITY) }

    when (currentStep) {
        OnboardingStep.INTENSITY -> IntensityScreen(
            onNext = { selectedIntensity ->
                onOnboardingComplete(
                    OnboardingResult(intensity = selectedIntensity)
                )
            }
        )
    }
}
