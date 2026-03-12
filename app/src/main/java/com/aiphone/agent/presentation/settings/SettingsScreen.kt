package com.aiphone.agent.presentation.settings
import androidx.compose.foundation.*; import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*; import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*; import androidx.compose.runtime.*
import androidx.compose.ui.*; import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*; import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation; import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiphone.agent.domain.model.ProviderType
import com.aiphone.agent.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)) },
        containerColor = Background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProviderSection(state = state, onSelectProvider = viewModel::setProvider, onSetApiKey = viewModel::setApiKey, onSetModel = viewModel::setModel)
            SandboxSection(state = state, onToggle = viewModel::setSandbox, onSetFolder = viewModel::setSandboxFolder)
            StatsSection(state = state)
        }
    }
}

@Composable
private fun SectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OnSurface)
            }
            Divider(color = OnSurfaceVariant.copy(alpha = 0.1f))
            content()
        }
    }
}

@Composable
private fun ProviderSection(state: SettingsUiState, onSelectProvider: (ProviderType) -> Unit, onSetApiKey: (ProviderType, String) -> Unit, onSetModel: (ProviderType, String) -> Unit) {
    SectionCard("AI Provider", Icons.Filled.SmartToy) {
        Text("Active Provider", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProviderType.values().forEach { p ->
                FilterChip(
                    selected = state.selectedProvider == p,
                    onClick = { onSelectProvider(p) },
                    label = { Text(p.displayName, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandPrimary, selectedLabelColor = Color.White)
                )
            }
        }
        val currentProvider = state.selectedProvider
        var keyVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = state.apiKeys[currentProvider] ?: "",
            onValueChange = { onSetApiKey(currentProvider, it) },
            label = { Text("${currentProvider.displayName} API Key") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = { keyVisible = !keyVisible }) { Icon(if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = OnSurfaceVariant) } },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, unfocusedBorderColor = SurfaceVariant, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface),
            shape = RoundedCornerShape(12.dp), singleLine = true
        )
        if (currentProvider.availableModels.size > 1) {
            Text("Model", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            val models = currentProvider.availableModels
            val selected = state.selectedModels[currentProvider] ?: currentProvider.defaultModel
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, unfocusedBorderColor = SurfaceVariant, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface),
                    shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(SurfaceContainer)) {
                    models.forEach { model -> DropdownMenuItem(text = { Text(model, style = MaterialTheme.typography.bodySmall, color = OnSurface) }, onClick = { onSetModel(currentProvider, model); expanded = false }) }
                }
            }
        }
    }
}

@Composable
private fun SandboxSection(state: SettingsUiState, onToggle: (Boolean) -> Unit, onSetFolder: (String) -> Unit) {
    SectionCard("Sandbox Mode", Icons.Filled.Security) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Restrict file operations", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                Text("Limits access to a selected folder only", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
            Switch(checked = state.sandboxEnabled, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandPrimary))
        }
        if (state.sandboxEnabled) {
            OutlinedTextField(value = state.sandboxFolder, onValueChange = onSetFolder, label = { Text("Sandbox Folder Path") },
                modifier = Modifier.fillMaxWidth(), placeholder = { Text("/storage/emulated/0/AIAgent") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, unfocusedBorderColor = SurfaceVariant, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface),
                shape = RoundedCornerShape(12.dp), singleLine = true)
        }
    }
}

@Composable
private fun StatsSection(state: SettingsUiState) {
    SectionCard("Usage Stats", Icons.Filled.Analytics) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("Total Tokens Used", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant); Text("${state.totalTokens}", style = MaterialTheme.typography.titleMedium, color = OnSurface, fontWeight = FontWeight.Bold) }
        }
    }
}