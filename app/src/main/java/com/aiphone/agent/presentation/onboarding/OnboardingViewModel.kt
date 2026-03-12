package com.aiphone.agent.presentation.onboarding
import androidx.lifecycle.ViewModel
import com.aiphone.agent.data.local.preferences.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel; import javax.inject.Inject
@HiltViewModel
class OnboardingViewModel @Inject constructor(private val securePreferences: SecurePreferences) : ViewModel() {
    fun completeOnboarding() = securePreferences.setOnboardingComplete(true)
}