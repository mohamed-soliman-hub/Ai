package com.aiphone.agent.presentation.settings
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import com.aiphone.agent.data.local.preferences.SecurePreferences
import com.aiphone.agent.domain.model.ProviderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch; import javax.inject.Inject

data class SettingsUiState(
    val selectedProvider: ProviderType = ProviderType.OPENAI,
    val apiKeys: Map<ProviderType, String> = emptyMap(),
    val selectedModels: Map<ProviderType, String> = emptyMap(),
    val sandboxEnabled: Boolean = false,
    val sandboxFolder: String = "",
    val totalTokens: Long = 0L,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(private val prefs: SecurePreferences) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init { loadSettings() }

    private fun loadSettings() {
        val keys = ProviderType.values().associateWith { prefs.getApiKey(it) }
        val models = ProviderType.values().associateWith { prefs.getSelectedModel(it) }
        _state.update { it.copy(selectedProvider = prefs.getSelectedProvider(), apiKeys = keys, selectedModels = models,
            sandboxEnabled = prefs.isSandboxEnabled(), sandboxFolder = prefs.getSandboxFolder(), totalTokens = prefs.getTotalTokensUsed()) }
    }

    fun setProvider(p: ProviderType) { prefs.setSelectedProvider(p); _state.update { it.copy(selectedProvider = p) } }
    fun setApiKey(p: ProviderType, key: String) {
        prefs.setApiKey(p, key)
        _state.update { it.copy(apiKeys = it.apiKeys + (p to key)) }
    }
    fun setModel(p: ProviderType, model: String) {
        prefs.setSelectedModel(p, model)
        _state.update { it.copy(selectedModels = it.selectedModels + (p to model)) }
    }
    fun setSandbox(enabled: Boolean) { prefs.setSandboxEnabled(enabled); _state.update { it.copy(sandboxEnabled = enabled) } }
    fun setSandboxFolder(f: String) { prefs.setSandboxFolder(f); _state.update { it.copy(sandboxFolder = f) } }
}