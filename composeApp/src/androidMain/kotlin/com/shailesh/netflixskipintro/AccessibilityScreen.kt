package com.shailesh.netflixskipintro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
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
    val buttonTexts by viewModel.buttonTexts.collectAsState()
    val autoClickEnabled by viewModel.autoClickEnabled.collectAsState()
    val enabledApps by viewModel.enabledApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val showNotifications by viewModel.showNotifications.collectAsState()
    
    var buttonTextToClick by remember { mutableStateOf("") }
    var ignoreCase by remember { mutableStateOf(true) }
    var newButtonText by remember { mutableStateOf("") }
    var newAppPackage by remember { mutableStateOf("") }
    var newAppName by remember { mutableStateOf("") }
    var showAddAppDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.checkServiceStatus()
    }
    
    LaunchedEffect(isServiceEnabled) {
        if (isServiceEnabled) {
            viewModel.checkServiceStatus()
        }
    }
    
    // Add App Dialog
    if (showAddAppDialog) {
        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            title = { Text("Add Custom App") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newAppPackage,
                        onValueChange = { newAppPackage = it },
                        label = { Text("Package Name") },
                        placeholder = { Text("com.example.app") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAppName,
                        onValueChange = { newAppName = it },
                        label = { Text("App Name") },
                        placeholder = { Text("My App") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newAppPackage.isNotBlank() && newAppName.isNotBlank()) {
                            viewModel.addCustomApp(newAppPackage.trim(), newAppName.trim())
                            newAppPackage = ""
                            newAppName = ""
                            showAddAppDialog = false
                        }
                    },
                    enabled = newAppPackage.isNotBlank() && newAppName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
            // Service Status
            item {
                ServiceStatusCard(
                    isEnabled = isServiceEnabled,
                    onOpenSettings = { 
                        viewModel.openAccessibilitySettings(context)
                    }
                )
            }
            
            // Auto-Click Toggle
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Auto-Click Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Automatically click buttons when detected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoClickEnabled,
                                onCheckedChange = { 
                                    viewModel.setAutoClickEnabled(it)
                                },
                                enabled = isServiceEnabled
                            )
                        }
                        
                        HorizontalDivider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Show Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Notify when apps are monitored",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = showNotifications,
                                onCheckedChange = { 
                                    viewModel.setShowNotifications(it)
                                },
                                enabled = isServiceEnabled
                            )
                        }
                    }
                }
            }
            
            // Enabled Apps Configuration
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
                                "Enabled Apps",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Button(
                                onClick = { showAddAppDialog = true },
                                enabled = isServiceEnabled,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add App", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        Text(
                            "Select apps where auto-click should work:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        allApps.forEach { (packageName, appName) ->
                            AppToggleItem(
                                appName = appName,
                                packageName = packageName,
                                isEnabled = enabledApps.contains(packageName),
                                isCustom = viewModel.isCustomApp(packageName),
                                onToggle = { viewModel.toggleAppEnabled(packageName) },
                                onRemove = { viewModel.removeApp(packageName) },
                                serviceEnabled = isServiceEnabled
                            )
                        }
                    }
                }
            }
            
            // Instructions
            item {
                InstructionsCard()
            }
            
            // Button Texts Management
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
                            "Auto-Click Button Texts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            "These texts will be automatically detected and clicked:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (buttonTexts.isEmpty()) {
                            Text(
                                "No button texts configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            buttonTexts.forEach { text ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeButtonText(text) },
                                        enabled = isServiceEnabled
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newButtonText,
                                onValueChange = { newButtonText = it },
                                label = { Text("Add button text") },
                                placeholder = { Text("e.g., Skip Intro") },
                                modifier = Modifier.weight(1f),
                                enabled = isServiceEnabled,
                                singleLine = true
                            )
                            
                            IconButton(
                                onClick = {
                                    if (newButtonText.isNotBlank()) {
                                        viewModel.addButtonText(newButtonText.trim())
                                        newButtonText = ""
                                    }
                                },
                                enabled = isServiceEnabled && newButtonText.isNotBlank()
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = if (newButtonText.isNotBlank()) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Manual Click
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
                            placeholder = { Text("Enter text to search") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isServiceEnabled
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = ignoreCase,
                                onCheckedChange = { ignoreCase = it },
                                enabled = isServiceEnabled
                            )
                            Text(
                                "Ignore case (recommended)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Button(
                            onClick = { 
                                viewModel.clickButton(buttonTextToClick, ignoreCase)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isServiceEnabled && buttonTextToClick.isNotBlank()
                        ) {
                            Text("Click Button")
                        }
                    }
                }
            }
            
            // Detected Buttons
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
fun AppToggleItem(
    appName: String,
    packageName: String,
    isEnabled: Boolean,
    isCustom: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    serviceEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = serviceEnabled) { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal
                )
                if (isCustom) {
                    Text(
                        packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() },
                    enabled = serviceEnabled
                )
                
                if (isCustom) {
                    IconButton(
                        onClick = onRemove,
                        enabled = serviceEnabled
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
                "2. Select which apps to monitor (Netflix, YouTube, etc.)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                "3. Configure button texts to auto-click",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                "4. Open enabled apps - you'll get a notification when monitoring starts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                "5. Service will auto-click configured buttons",
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
                    "${buttonInfo.className.substringAfterLast(".")} ${if (buttonInfo.isClickable) "✓" else ""}",
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
