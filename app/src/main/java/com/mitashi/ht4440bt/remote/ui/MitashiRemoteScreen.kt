package com.mitashi.ht4440bt.remote.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitashi.ht4440bt.remote.data.IrButtonCode
import com.mitashi.ht4440bt.remote.data.IrProtocolType
import com.mitashi.ht4440bt.remote.data.PresetProfiles
import com.mitashi.ht4440bt.remote.data.RemoteButton
import com.mitashi.ht4440bt.remote.ui.theme.AccentTeal
import com.mitashi.ht4440bt.remote.ui.theme.PowerRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MitashiRemoteScreen(viewModel: RemoteViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showPresetsDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var editingButton by remember { mutableStateOf<RemoteButton?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        state.isTransmitting -> PowerRed
                                        state.hasIrBlaster -> AccentTeal
                                        else -> Color(0xFFFFB300)
                                    }
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mitashi HT4440BT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.irHardwareSummary,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showPresetsDialog = true }) {
                        Icon(Icons.Rounded.SettingsRemote, contentDescription = "Presets")
                    }
                    IconButton(onClick = { showScannerDialog = true }) {
                        Icon(Icons.Rounded.Tune, contentDescription = "IR Scanner")
                    }
                    IconButton(onClick = { viewModel.toggleDarkTheme() }) {
                        Icon(
                            imageVector = if (state.isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle theme"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live status strip
            Text(
                text = state.lastSentLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Physical Remote Shell (1:1 button layout of Mitashi HT4440BT)
            Card(
                modifier = Modifier.widthIn(max = 340.dp).fillMaxWidth(),
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Top Red Power Button
                    PowerKey(
                        onClick = { viewModel.sendButton(RemoteButton.POWER) },
                        onLongClick = { editingButton = RemoteButton.POWER }
                    )

                    Spacer(Modifier.height(16.dp))

                    // 2. Utility Row: INPUT, SAVE, SLEEP, MUTE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            RemoteButton.INPUT,
                            RemoteButton.SAVE,
                            RemoteButton.SLEEP,
                            RemoteButton.MUTE
                        ).forEach { btn ->
                            PillKey(
                                label = btn.label,
                                isDark = state.isDarkTheme,
                                onClick = { viewModel.sendButton(btn) },
                                onLongClick = { editingButton = btn },
                                modifier = Modifier.weight(1f),
                                height = 34.dp,
                                fontSize = 10
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // 3. EQ + Circular D-Pad (TUNE+, CH-, AUTO, CH+, TUNE-) + DISP
                    CircularDPadCluster(
                        isDark = state.isDarkTheme,
                        onPress = { viewModel.sendButton(it) },
                        onLongPress = { editingButton = it }
                    )

                    Spacer(Modifier.height(18.dp))

                    // 4. Audio Control Section: SW Rocker | TRE+ / TRE- | VOL Rocker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerticalRockerKey(
                            centerLabel = "SW",
                            isDark = state.isDarkTheme,
                            onPlusClick = { viewModel.sendButton(RemoteButton.SW_UP) },
                            onPlusLongClick = { editingButton = RemoteButton.SW_UP },
                            onMinusClick = { viewModel.sendButton(RemoteButton.SW_DOWN) },
                            onMinusLongClick = { editingButton = RemoteButton.SW_DOWN }
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.width(84.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TRE+",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                PillKey(
                                    label = "+",
                                    isDark = state.isDarkTheme,
                                    onClick = { viewModel.sendButton(RemoteButton.TRE_UP) },
                                    onLongClick = { editingButton = RemoteButton.TRE_UP },
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 32.dp,
                                    fontSize = 14
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TRE−",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                PillKey(
                                    label = "−",
                                    isDark = state.isDarkTheme,
                                    onClick = { viewModel.sendButton(RemoteButton.TRE_DOWN) },
                                    onLongClick = { editingButton = RemoteButton.TRE_DOWN },
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 32.dp,
                                    fontSize = 14
                                )
                            }
                        }

                        VerticalRockerKey(
                            centerLabel = "VOL",
                            isDark = state.isDarkTheme,
                            onPlusClick = { viewModel.sendButton(RemoteButton.VOL_UP) },
                            onPlusLongClick = { editingButton = RemoteButton.VOL_UP },
                            onMinusClick = { viewModel.sendButton(RemoteButton.VOL_DOWN) },
                            onMinusLongClick = { editingButton = RemoteButton.VOL_DOWN }
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    // 5. Numeric Keypad (1-9, REPEAT, 0, RESET)
                    val numRows = listOf(
                        listOf(RemoteButton.NUM_1, RemoteButton.NUM_2, RemoteButton.NUM_3),
                        listOf(RemoteButton.NUM_4, RemoteButton.NUM_5, RemoteButton.NUM_6),
                        listOf(RemoteButton.NUM_7, RemoteButton.NUM_8, RemoteButton.NUM_9),
                        listOf(RemoteButton.REPEAT, RemoteButton.NUM_0, RemoteButton.RESET)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        numRows.forEach { rowButtons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowButtons.forEach { btn ->
                                    PillKey(
                                        label = btn.label,
                                        isDark = state.isDarkTheme,
                                        onClick = { viewModel.sendButton(btn) },
                                        onLongClick = { editingButton = btn },
                                        modifier = Modifier.weight(1f),
                                        height = 36.dp,
                                        fontSize = if (btn.label.length > 2) 10 else 13
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 6. Mitashi Brand Footer
                    Text(
                        text = "MITASHI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "Long-press any button to edit its IR hex code",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showPresetsDialog) {
        PresetsAndImportDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { showPresetsDialog = false }
        )
    }

    if (showScannerDialog) {
        IrScannerDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = {
                viewModel.stopAutoScan()
                showScannerDialog = false
            }
        )
    }

    editingButton?.let { btn ->
        val currentCode = state.effectiveCodes[btn] ?: IrButtonCode()
        ButtonCodeEditorDialog(
            button = btn,
            initialCode = currentCode,
            onTest = { code -> viewModel.transmitCode(btn.label, code) },
            onSave = { code ->
                viewModel.updateButtonCode(btn, code)
                editingButton = null
            },
            onDismiss = { editingButton = null }
        )
    }
}

@Composable
private fun PresetsAndImportDialog(
    state: RemoteUiState,
    viewModel: RemoteViewModel,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var importText by remember { mutableStateOf("") }
    var importFeedback by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("IR Presets & Profile Backup") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Select an OEM NEC preset to test with your Mitashi HT4440BT:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PresetProfiles.allPresets.forEach { preset ->
                    FilterChip(
                        selected = preset.id == state.activePreset.id,
                        onClick = { viewModel.selectPreset(preset) },
                        label = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(preset.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(preset.subtitle, fontSize = 11.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(viewModel.exportProfileJson()))
                            importFeedback = "Copied profile JSON to clipboard."
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy JSON", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.resetCustomOverrides()
                            importFeedback = "Cleared custom button overrides."
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Edits", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    label = { Text("Paste Profile JSON or Flipper .ir") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { importFeedback = viewModel.importFromText(importText) },
                    enabled = importText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import IR Codes")
                }
                importFeedback?.let {
                    Text(it, fontSize = 12.sp, color = AccentTeal)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun IrScannerDialog(
    state: RemoteUiState,
    viewModel: RemoteViewModel,
    onDismiss: () -> Unit
) {
    var addrHex by remember(state.scannerAddress) {
        mutableStateOf("%02X".format(state.scannerAddress))
    }
    var cmdHex by remember(state.scannerCommand) {
        mutableStateOf("%02X".format(state.scannerCommand))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEC IR Code Scanner") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Point your phone's IR blaster at the Mitashi HT4440BT. Step through NEC commands or run Auto-Scan. When the speaker reacts, tap Save to Button.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = addrHex,
                        onValueChange = { v ->
                            addrHex = v.uppercase().take(4)
                            addrHex.toIntOrNull(16)?.let { viewModel.setScannerAddress(it) }
                        },
                        label = { Text("Addr Hex") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cmdHex,
                        onValueChange = { v ->
                            cmdHex = v.uppercase().take(2)
                            cmdHex.toIntOrNull(16)?.let { viewModel.setScannerCommand(it) }
                        },
                        label = { Text("Cmd Hex") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.setScannerCommand((state.scannerCommand - 1) and 0xFF)
                            viewModel.testCurrentScannerCode()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Prev −1") }

                    Button(
                        onClick = { viewModel.testCurrentScannerCode() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Blast") }

                    OutlinedButton(
                        onClick = {
                            viewModel.setScannerCommand((state.scannerCommand + 1) and 0xFF)
                            viewModel.testCurrentScannerCode()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Next +1") }
                }

                Button(
                    onClick = {
                        if (state.isAutoScanning) viewModel.stopAutoScan()
                        else viewModel.startAutoScanCommands()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isAutoScanning) "Stop Auto-Scan" else "Start Auto-Scan (0x00 → 0xFF)")
                }

                OutlinedButton(
                    onClick = {
                        viewModel.applyAddressToAllButtons(
                            address = state.scannerAddress,
                            protocol = state.scannerProtocol
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Address 0x%02X to All Buttons".format(state.scannerAddress))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save to: ${state.scannerTargetButton.label}", fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = {
                        val entries = RemoteButton.entries
                        val nextIdx = (entries.indexOf(state.scannerTargetButton) + 1) % entries.size
                        viewModel.setScannerTargetButton(entries[nextIdx])
                    }) {
                        Text("Change Button")
                    }
                }

                Button(
                    onClick = { viewModel.saveCurrentScannerCodeToTarget() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Code to ${state.scannerTargetButton.label}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ButtonCodeEditorDialog(
    button: RemoteButton,
    initialCode: IrButtonCode,
    onTest: (IrButtonCode) -> Unit,
    onSave: (IrButtonCode) -> Unit,
    onDismiss: () -> Unit
) {
    var protocol by remember { mutableStateOf(initialCode.protocol) }
    var addrText by remember { mutableStateOf("%02X".format(initialCode.address)) }
    var cmdText by remember { mutableStateOf("%02X".format(initialCode.command)) }
    var prontoText by remember { mutableStateOf(initialCode.prontoHex) }

    fun buildCode(): IrButtonCode = IrButtonCode(
        protocol = protocol,
        frequencyHz = 38_000,
        address = addrText.toIntOrNull(16) ?: 0,
        command = cmdText.toIntOrNull(16) ?: 0,
        prontoHex = prontoText.trim()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${button.label}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(button.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(IrProtocolType.NEC, IrProtocolType.NEC_EXT, IrProtocolType.PRONTO_HEX).forEach { p ->
                        FilterChip(
                            selected = protocol == p,
                            onClick = { protocol = p },
                            label = { Text(p.name, fontSize = 11.sp) }
                        )
                    }
                }

                if (protocol == IrProtocolType.PRONTO_HEX) {
                    OutlinedTextField(
                        value = prontoText,
                        onValueChange = { prontoText = it },
                        label = { Text("Pronto Hex (0000 006D ...)") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = addrText,
                            onValueChange = { addrText = it.uppercase().take(4) },
                            label = { Text("Address Hex") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cmdText,
                            onValueChange = { cmdText = it.uppercase().take(2) },
                            label = { Text("Command Hex") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onTest(buildCode()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test IR Signal")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(buildCode()) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
