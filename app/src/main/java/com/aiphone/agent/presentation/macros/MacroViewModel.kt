package com.aiphone.agent.presentation.macros
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import com.aiphone.agent.core.macro.MacroEngine; import com.aiphone.agent.core.macro.MacroResult
import com.aiphone.agent.domain.model.Macro; import com.aiphone.agent.domain.repository.IMacroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch; import javax.inject.Inject

data class MacroUiState(val macros: List<Macro> = emptyList(), val runningMacroId: String? = null, val runStatus: String = "", val error: String? = null)

@HiltViewModel
class MacroViewModel @Inject constructor(private val macroRepository: IMacroRepository, private val macroEngine: MacroEngine) : ViewModel() {
    private val _state = MutableStateFlow(MacroUiState())
    val state: StateFlow<MacroUiState> = _state.asStateFlow()
    init { viewModelScope.launch { macroRepository.getAllMacros().collect { _state.update { s -> s.copy(macros = it) } } } }
    fun deleteMacro(id: String) = viewModelScope.launch { macroRepository.deleteMacro(id) }
    fun runMacro(macro: Macro) = viewModelScope.launch {
        _state.update { it.copy(runningMacroId = macro.id) }
        macroEngine.playMacro(macro).collect { result ->
            when (result) {
                is MacroResult.StepStarted  -> _state.update { it.copy(runStatus = "Running: ${result.step.tool}") }
                is MacroResult.Completed    -> _state.update { it.copy(runningMacroId = null, runStatus = "Completed!") }
                is MacroResult.Failed       -> _state.update { it.copy(runningMacroId = null, error = result.message) }
                else -> {}
            }
        }
    }
    fun clearError() = _state.update { it.copy(error = null) }
}