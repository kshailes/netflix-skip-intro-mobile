package com.shailesh.netflixskipintro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(viewModel: AccessibilityViewModel = viewModel()) {
    val context = LocalContext.current
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsState()
    val detectedButtons by viewModel.detectedButtons.collectAsState()
    
    var buttonTextToClick by remember { mutableStateOf("Skip Intro") }
    
    LaunchedEffect(Unit) {
        viewModel.checkServiceStatus()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Netflix Skip Intro") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Service Status Card
            item {
                ServiceStatusCard(
                    isEnabled = isServiceEnabled,
                    onOpenSettings = { 
                        viewModel.openAccessibilitySettings(context)
                    }
                )
            }
            
            // Instructions Card
            item {
                InstructionsCard()
            }
            
            // Manual Click Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Manual Button Click",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = buttonTextToClick,
                            onValueChange = { buttonTextToClick = it },
                            label = { Text("Button Text") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isServiceEnabled
                        )
                        
                        Button(
                            onClick = { 
                                viewModel.clickButton(buttonTextToClick)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isServiceEnabled
                        ) {
                            Text("Click Button")
                        }
                    }
                }
            }
            
            // Detected Buttons Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Detected Buttons",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Button(
                                onClick = { viewModel.refreshDetectedButtons() },
                                enabled = isServiceEnabled,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Scan", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        if (detectedButtons.isEmpty()) {
                            Text(
                                "No buttons detected. Open another app and tap 'Scan'.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // List detected buttons
            items(detectedButtons) { buttonInfo ->
                DetectedButtonCard(
                    buttonInfo = buttonInfo,
                    onClickButton = { 
                        viewModel.clickButton(buttonInfo.text.ifEmpty { buttonInfo.contentDescription })
                    }
                )
            }
        }
    }
}

@Composable
fun ServiceStatusCard(
    isEnabled: Boolean,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isEnabled) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        "Accessibility Service",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    if (isEnabled) "Service is active" else "Service is not enabled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isEnabled) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            if (!isEnabled) {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Enable")
                }
            }
        }
    }
}

@Composable
fun InstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "How to Use",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                "1. Enable the Accessibility Service in Settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                "2. Open Netflix or any app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                "3. The service will automatically detect and click 'Skip Intro' buttons",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                "4. Or use the Manual Click feature to click any button",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun DetectedButtonCard(
    buttonInfo: AutoClickAccessibilityService.ButtonInfo,
    onClickButton: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (buttonInfo.text.isNotEmpty()) {
                    Text(
                        buttonInfo.text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (buttonInfo.contentDescription.isNotEmpty()) {
                    Text(
                        buttonInfo.contentDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    buttonInfo.className.substringAfterLast("."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            FilledTonalButton(
                onClick = onClickButton,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Click", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

