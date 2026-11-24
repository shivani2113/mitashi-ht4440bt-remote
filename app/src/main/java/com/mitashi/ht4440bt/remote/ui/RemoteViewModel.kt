package com.mitashi.ht4440bt.remote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mitashi.ht4440bt.remote.data.IrButtonCode
import com.mitashi.ht4440bt.remote.data.IrProtocolType
import com.mitashi.ht4440bt.remote.data.PresetProfiles
import com.mitashi.ht4440bt.remote.data.RemoteButton
import com.mitashi.ht4440bt.remote.data.RemoteProfile
import com.mitashi.ht4440bt.remote.data.RemoteProfileRepository
import com.mitashi.ht4440bt.remote.ir.IrBlasterController
import com.mitashi.ht4440bt.remote.ir.IrProtocolEncoder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RemoteUiState(
    val hasIrBlaster: Boolean = false,
    val irHardwareSummary: String = "Checking IR hardware…",
    val activePreset: RemoteProfile = PresetProfiles.allPresets.first(),
    val effectiveCodes: Map<RemoteButton, IrButtonCode> = PresetProfiles.allPresets.first().buttonCodes,
    val customOverrideCount: Int = 0,
    val lastSentLabel: String = "Ready — tap any button",
    val isTransmitting: Boolean = false,
    val isDarkTheme: Boolean = true,
    val hapticEnabled: Boolean = true,
    // IR Scanner / Calibration state
    val scannerProtocol: IrProtocolType = IrProtocolType.NEC,
    val scannerAddress: Int = 0x00,
    val scannerCommand: Int = 0x45,
    val isAutoScanning: Boolean = false,
    val scannerTargetButton: RemoteButton = RemoteButton.POWER
)

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RemoteProfileRepository(application)
    private val irController = IrBlasterController(application)

    private var customOverrides: MutableMap<RemoteButton, IrButtonCode> =
        repository.loadCustomOverrides().toMutableMap()

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null
    private var ledFlashJob: Job? = null

    private fun buildInitialState(): RemoteUiState {
        val presetId = repository.loadActivePresetId()
        val preset = PresetProfiles.allPresets.find { it.id == presetId }
            ?: PresetProfiles.allPresets.first()
        return RemoteUiState(
            hasIrBlaster = irController.hasIrEmitter,
            irHardwareSummary = irController.getSupportedFrequencySummary(),
            activePreset = preset,
            effectiveCodes = mergeCodes(preset, customOverrides),
            customOverrideCount = customOverrides.size,
            isDarkTheme = repository.isDarkTheme(),
            hapticEnabled = repository.isHapticEnabled(),
            scannerAddress = preset.defaultAddress
        )
    }

    private fun mergeCodes(
        preset: RemoteProfile,
        overrides: Map<RemoteButton, IrButtonCode>
    ): Map<RemoteButton, IrButtonCode> {
        val merged = preset.buttonCodes.toMutableMap()
        merged.putAll(overrides)
        return merged
    }

    fun sendButton(button: RemoteButton) {
        val code = _uiState.value.effectiveCodes[button] ?: return
        transmitCode(button.label, code)
    }

    fun transmitCode(label: String, code: IrButtonCode) {
        viewModelScope.launch {
            flashIrLed()
            val result = irController.transmit(code, vibrate = _uiState.value.hapticEnabled)
            val status = result.fold(
                onSuccess = {
                    val hwTag = if (_uiState.value.hasIrBlaster) "Sent" else "Simulated (no IR HW)"
                    "$hwTag $label • ${code.displaySummary()}"
                },
                onFailure = { err ->
                    "Error sending $label: ${err.localizedMessage ?: "Invalid IR code"}"
                }
            )
            _uiState.update { it.copy(lastSentLabel = status) }
        }
    }

    private fun flashIrLed() {
        ledFlashJob?.cancel()
        ledFlashJob = viewModelScope.launch {
            _uiState.update { it.copy(isTransmitting = true) }
            delay(180)
            _uiState.update { it.copy(isTransmitting = false) }
        }
    }

    fun selectPreset(preset: RemoteProfile) {
        repository.saveActivePresetId(preset.id)
        _uiState.update {
            it.copy(
                activePreset = preset,
                effectiveCodes = mergeCodes(preset, customOverrides),
                scannerAddress = preset.defaultAddress,
                scannerProtocol = preset.protocol,
                lastSentLabel = "Switched to ${preset.name}"
            )
        }
    }

    fun updateButtonCode(button: RemoteButton, newCode: IrButtonCode) {
        customOverrides[button] = newCode
        repository.saveCustomOverrides(customOverrides)
        _uiState.update { state ->
            state.copy(
                effectiveCodes = mergeCodes(state.activePreset, customOverrides),
                customOverrideCount = customOverrides.size,
                lastSentLabel = "Saved ${button.label} -> ${newCode.displaySummary()}"
            )
        }
    }

    fun applyAddressToAllButtons(address: Int, protocol: IrProtocolType) {
        val base = _uiState.value.effectiveCodes
        for ((btn, existing) in base) {
            customOverrides[btn] = existing.copy(
                protocol = protocol,
                address = address
            )
        }
        repository.saveCustomOverrides(customOverrides)
        _uiState.update { state ->
            state.copy(
                effectiveCodes = mergeCodes(state.activePreset, customOverrides),
                customOverrideCount = customOverrides.size,
                scannerAddress = address,
                scannerProtocol = protocol,
                lastSentLabel = "Applied address 0x%02X to all 32 buttons".format(address)
            )
        }
    }

    fun resetCustomOverrides() {
        customOverrides.clear()
        repository.clearCustomOverrides()
        _uiState.update { state ->
            state.copy(
                effectiveCodes = state.activePreset.buttonCodes,
                customOverrideCount = 0,
                lastSentLabel = "Reset all buttons to ${state.activePreset.name}"
            )
        }
    }

    fun toggleDarkTheme() {
        val next = !_uiState.value.isDarkTheme
        repository.saveDarkTheme(next)
        _uiState.update { it.copy(isDarkTheme = next) }
    }

    fun toggleHaptic() {
        val next = !_uiState.value.hapticEnabled
        repository.saveHapticEnabled(next)
        _uiState.update { it.copy(hapticEnabled = next) }
    }

    // --- IR Code Scanner / Calibrator ---

    fun setScannerAddress(addr: Int) {
        _uiState.update { it.copy(scannerAddress = addr and 0xFFFF) }
    }

    fun setScannerCommand(cmd: Int) {
        _uiState.update { it.copy(scannerCommand = cmd and 0xFF) }
    }

    fun setScannerTargetButton(button: RemoteButton) {
        _uiState.update { it.copy(scannerTargetButton = button) }
    }

    fun testCurrentScannerCode() {
        val state = _uiState.value
        val code = IrButtonCode(
            protocol = state.scannerProtocol,
            frequencyHz = 38_000,
            address = state.scannerAddress,
            command = state.scannerCommand
        )
        transmitCode("SCAN", code)
    }

    fun saveCurrentScannerCodeToTarget() {
        val state = _uiState.value
        val code = IrButtonCode(
            protocol = state.scannerProtocol,
            frequencyHz = 38_000,
            address = state.scannerAddress,
            command = state.scannerCommand
        )
        updateButtonCode(state.scannerTargetButton, code)
    }

    fun startAutoScanCommands(delayMs: Long = 650L) {
        stopAutoScan()
        scanJob = viewModelScope.launch {
            _uiState.update { it.copy(isAutoScanning = true) }
            var cmd = _uiState.value.scannerCommand
            while (isActive) {
                val state = _uiState.value
                val code = IrButtonCode(
                    protocol = state.scannerProtocol,
                    frequencyHz = 38_000,
                    address = state.scannerAddress,
                    command = cmd
                )
                _uiState.update { it.copy(scannerCommand = cmd) }
                transmitCode("SCAN 0x%02X".format(cmd), code)
                delay(delayMs)
                cmd = (cmd + 1) and 0xFF
            }
        }
    }

    fun stopAutoScan() {
        scanJob?.cancel()
        scanJob = null
        _uiState.update { it.copy(isAutoScanning = false) }
    }

    fun exportProfileJson(): String = repository.exportProfileJson(_uiState.value.effectiveCodes)

    fun importFromText(rawText: String): String {
        return try {
            val trimmed = rawText.trim()
            if (trimmed.startsWith("{")) {
                val imported = repository.importProfileJson(trimmed)
                if (imported.isEmpty()) return "No matching buttons found in JSON."
                customOverrides.putAll(imported)
                repository.saveCustomOverrides(customOverrides)
                _uiState.update { state ->
                    state.copy(
                        effectiveCodes = mergeCodes(state.activePreset, customOverrides),
                        customOverrideCount = customOverrides.size,
                        lastSentLabel = "Imported ${imported.size} buttons from JSON"
                    )
                }
                "Imported ${imported.size} buttons from JSON."
            } else {
                val flipperMap = IrProtocolEncoder.parseFlipperIrFile(trimmed)
                if (flipperMap.isEmpty()) return "No valid Flipper .ir signals found."
                var matched = 0
                for ((name, code) in flipperMap) {
                    val normalized = name.uppercase().replace(" ", "_")
                    val match = RemoteButton.entries.find {
                        it.id == normalized || it.label.uppercase().replace(" ", "_") == normalized
                    }
                    if (match != null) {
                        customOverrides[match] = code
                        matched++
                    }
                }
                repository.saveCustomOverrides(customOverrides)
                _uiState.update { state ->
                    state.copy(
                        effectiveCodes = mergeCodes(state.activePreset, customOverrides),
                        customOverrideCount = customOverrides.size,
                        lastSentLabel = "Imported $matched buttons from Flipper .ir"
                    )
                }
                "Imported $matched buttons from Flipper .ir file."
            }
        } catch (e: Throwable) {
            "Import failed: ${e.localizedMessage ?: "Invalid format"}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoScan()
    }
}
