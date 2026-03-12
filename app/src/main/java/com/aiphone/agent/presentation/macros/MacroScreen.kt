package com.aiphone.agent.presentation.macros
import androidx.compose.foundation.*; import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*; import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*; import androidx.compose.runtime.*
import androidx.compose.ui.*; import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*; import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiphone.agent.domain.model.Macro
import com.aiphone.agent.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroScreen(onBack: () -> Unit, onRunMacro: (Macro) -> Unit, viewModel: MacroViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Macros", fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)) },
        containerColor = Background
    ) { padding ->
        if (state.macros.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.PlayCircleOutline, null, tint = OnSurfaceVariant, modifier = Modifier.size(64.dp))
                    Text("No macros yet", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                    Text("Create macros by recording tool sequences", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.macros) { macro -> MacroCard(macro = macro, isRunning = state.runningMacroId == macro.id, onRun = { viewModel.runMacro(macro) }, onDelete = { viewModel.deleteMacro(macro.id) }) }
            }
        }
    }
}

@Composable
private fun MacroCard(macro: Macro, isRunning: Boolean, onRun: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).background(BrandPrimary.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.PlayArrow, null, tint = BrandPrimary, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(macro.name, style = MaterialTheme.typography.titleMedium, color = OnSurface, fontWeight = FontWeight.SemiBold)
                if (macro.description.isNotBlank()) Text(macro.description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                Text("${macro.steps.size} steps  •  Run ${macro.runCount}x", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.7f))
            }
            if (isRunning) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = BrandPrimary)
            else {
                IconButton(onClick = onRun) { Icon(Icons.Filled.PlayArrow, "Run", tint = BrandSecondary) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Delete", tint = AppError.copy(alpha = 0.7f)) }
            }
        }
    }
}