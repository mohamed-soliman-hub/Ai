package com.aiphone.agent.presentation.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiphone.agent.domain.model.*
import com.aiphone.agent.presentation.theme.*

@Composable
fun ChatScreen(
    conversationId: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToMacros: () -> Unit,
    onNewConversation: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.size - 1)
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBar(
                onSettings = onNavigateToSettings,
                onMacros = onNavigateToMacros,
                onNewChat = onNewConversation
            )
            // Messages list
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.messages.isEmpty() && !uiState.isProcessing) {
                    WelcomeScreen()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 2 }) {
                                MessageBubble(message = message)
                            }
                        }
                        if (uiState.isProcessing) {
                            item {
                                TypingIndicator(
                                    step = uiState.processingStep,
                                    tools = uiState.currentTools
                                )
                            }
                        }
                    }
                }
            }
            // Error snackbar
            AnimatedVisibility(visible = uiState.error != null) {
                uiState.error?.let { error ->
                    ErrorBanner(message = error, onDismiss = viewModel::clearError)
                }
            }
            // Input area
            ChatInputBar(
                isProcessing = uiState.isProcessing,
                onSend = viewModel::sendMessage
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(onSettings: () -> Unit, onMacros: () -> Unit, onNewChat: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(36.dp).background(Brush.linearGradient(listOf(BrandPrimary, BrandPrimaryDark)), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                Column {
                    Text("AI Phone Agent", style = MaterialTheme.typography.titleMedium, color = OnSurface, fontWeight = FontWeight.SemiBold)
                    Text("Ready", style = MaterialTheme.typography.labelSmall, color = BrandSecondary)
                }
            }
        },
        actions = {
            IconButton(onClick = onNewChat) { Icon(Icons.Filled.Add, "New Chat", tint = OnSurfaceVariant) }
            IconButton(onClick = onMacros) { Icon(Icons.Filled.PlayCircle, "Macros", tint = OnSurfaceVariant) }
            IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, "Settings", tint = OnSurfaceVariant) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.95f)),
        modifier = Modifier.drawBehind {
            drawLine(ToolBorder.copy(alpha = 0.3f), androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), 1f)
        }
    )
}

@Composable
private fun WelcomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(Brush.radialGradient(listOf(BrandPrimary.copy(alpha = 0.3f), Color.Transparent)), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.AutoAwesome, null, tint = BrandPrimary, modifier = Modifier.size(44.dp)) }
        Spacer(Modifier.height(24.dp))
        Text("Hello! I am your AI Phone Agent", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("I can manage your files, control apps, analyze images, and execute complex tasks — just ask.",
            style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        ExampleChips()
    }
}

@Composable
private fun ExampleChips() {
    val examples = listOf("List files in Downloads", "Open WhatsApp", "Find PDF files", "What apps are installed?")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        examples.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { example ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(example, style = MaterialTheme.typography.labelMedium) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceContainer),
                        border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = BrandPrimary.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(modifier = Modifier.size(32.dp).background(Brush.linearGradient(listOf(BrandPrimary, BrandPrimaryDark)), CircleShape).align(Alignment.Bottom),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start, modifier = Modifier.widthIn(max = 300.dp)) {
            Box(
                modifier = Modifier.background(
                    if (isUser) Brush.linearGradient(listOf(UserBubbleStart, UserBubbleEnd), end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY))
                    else Brush.linearGradient(listOf(AIBubbleSurface, AIBubbleSurface)),
                    RoundedCornerShape(18.dp, 18.dp, if (isUser) 4.dp else 18.dp, if (isUser) 18.dp else 4.dp)
                ).padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(message.content, style = MaterialTheme.typography.bodyMedium, color = if (isUser) Color.White else OnSurface)
            }
            if (message.toolResults.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                ToolResultsSummary(results = message.toolResults)
            }
            Spacer(Modifier.height(2.dp))
            Text(formatTime(message.timestamp), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.6f))
        }
        if (isUser) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun ToolResultsSummary(results: List<ToolResult>) {
    var expanded by remember { mutableStateOf(false) }
    val successCount = results.count { it.status == ToolResultStatus.SUCCESS }
    val failCount = results.count { it.status == ToolResultStatus.FAILURE }
    Column {
        OutlinedCard(
            onClick = { expanded = !expanded },
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = ToolBubble),
            border = androidx.compose.foundation.BorderStroke(1.dp, ToolBorder.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Terminal, null, tint = BrandSecondary, modifier = Modifier.size(14.dp))
                Text("${results.size} tool${if (results.size != 1) "s" else ""} executed  •  $successCount ok${if (failCount > 0) " / $failCount failed" else ""}",
                    style = MaterialTheme.typography.labelSmall, color = BrandSecondary)
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.widthIn(max = 280.dp).padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                results.forEach { result ->
                    ToolResultItem(result = result)
                }
            }
        }
    }
}

@Composable
private fun ToolResultItem(result: ToolResult) {
    val isSuccess = result.status == ToolResultStatus.SUCCESS
    Row(modifier = Modifier.background(ToolBubble, RoundedCornerShape(6.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
        Icon(if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error, null,
            tint = if (isSuccess) Success else AppError, modifier = Modifier.size(14.dp).padding(top = 1.dp))
        Column {
            Text(result.toolName, style = MaterialTheme.typography.labelSmall, color = if (isSuccess) BrandSecondary else AppError, fontWeight = FontWeight.SemiBold)
            if (result.output.isNotBlank()) {
                Text(result.output.take(120).let { if (result.output.length > 120) "$it..." else it },
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = OnSurface.copy(alpha = 0.8f))
            }
            if (!isSuccess && !result.errorMessage.isNullOrBlank()) {
                Text(result.errorMessage, style = MaterialTheme.typography.bodySmall, color = AppError.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun TypingIndicator(step: String, tools: List<String>) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "alpha")
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(32.dp).background(Brush.linearGradient(listOf(BrandPrimary, BrandPrimaryDark)), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.background(AIBubbleSurface, RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)).padding(12.dp, 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = BrandPrimary.copy(alpha = alpha))
                Text(step, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
            if (tools.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Tools: ${tools.takeLast(3).joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = BrandSecondary.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(AppError.copy(alpha = 0.1f)).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.ErrorOutline, null, tint = AppError, modifier = Modifier.size(18.dp))
        Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = AppError)
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Filled.Close, "Dismiss", tint = AppError, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ChatInputBar(isProcessing: Boolean, onSend: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val hasText = inputText.isNotBlank()
    Surface(color = Surface, tonalElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask me anything...", color = OnSurfaceVariant.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer,
                    focusedBorderColor = BrandPrimary.copy(alpha = 0.6f), unfocusedBorderColor = SurfaceVariant,
                    focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, cursorColor = BrandPrimary
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (hasText && !isProcessing) { onSend(inputText); inputText = "" } }),
                maxLines = 5,
                enabled = !isProcessing
            )
            Box(
                modifier = Modifier.size(48.dp).background(
                    if (hasText && !isProcessing) Brush.linearGradient(listOf(BrandPrimary, BrandPrimaryDark))
                    else Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainer)),
                    CircleShape
                ).clickable(enabled = hasText && !isProcessing) { onSend(inputText); inputText = "" },
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = BrandPrimary)
                else Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = if (hasText) Color.White else OnSurfaceVariant, modifier = Modifier.size(22.dp))
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
}