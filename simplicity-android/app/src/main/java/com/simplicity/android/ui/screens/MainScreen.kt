package com.simplicity.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplicity.android.data.model.Provider
import com.simplicity.android.data.model.Model
import com.simplicity.android.data.repository.ModelRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(repository: ModelRepository) {
    val scope = rememberCoroutineScope()
    
    var providers by remember { mutableStateOf<List<Provider>>(emptyList()) }
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var messages by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        providers = repository.discoverProviders()
        selectedProvider = providers.firstOrNull { it.isConnected }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simplicity AI") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            providers = repository.discoverProviders()
                            selectedProvider = providers.firstOrNull { it.isConnected }
                            isLoading = false
                        }
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Provider Status Cards
                Text(
                    text = "Providers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                providers.forEach { provider ->
                    ProviderCard(
                        provider = provider,
                        isSelected = selectedProvider?.id == provider.id,
                        onClick = {
                            selectedProvider = provider
                            selectedModel = provider.models.firstOrNull()?.name
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Model Selector
                if (selectedProvider != null && selectedProvider!!.models.isNotEmpty()) {
                    Text(
                        text = "Select Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedModel ?: "Select a model",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            selectedProvider!!.models.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model.name) },
                                    onClick = {
                                        selectedModel = model.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Chat Messages
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = false
                ) {
                    items(messages) { (role, content) ->
                        MessageBubble(role = role, content = content)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask something...") },
                        enabled = !isGenerating && selectedProvider?.isConnected == true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank() && selectedProvider != null && selectedModel != null) {
                                val userMessage = inputText
                                messages = messages + ("user" to userMessage)
                                inputText = ""
                                isGenerating = true
                                
                                scope.launch {
                                    val result = repository.generate(
                                        selectedProvider!!,
                                        selectedModel!!,
                                        userMessage
                                    )
                                    result.onSuccess { response ->
                                        messages = messages + ("assistant" to response)
                                    }.onFailure { error ->
                                        messages = messages + ("error" to error.message ?: "Error occurred")
                                    }
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = !isGenerating && selectedProvider?.isConnected == true
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderCard(
    provider: Provider,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = provider.baseUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (provider.isConnected) {
                    Text(
                        text = "${provider.models.size} models",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = provider.error ?: "Not connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Icon(
                imageVector = if (provider.isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (provider.isConnected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun MessageBubble(role: String, content: String) {
    val isUser = role == "user"
    val backgroundColor = when (role) {
        "user" -> MaterialTheme.colorScheme.primary
        "assistant" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val textColor = when (role) {
        "user" -> MaterialTheme.colorScheme.onPrimary
        "assistant" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Text(
                text = content,
                color = textColor,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
