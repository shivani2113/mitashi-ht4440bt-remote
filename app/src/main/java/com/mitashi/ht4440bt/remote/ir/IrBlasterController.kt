package com.mitashi.ht4440bt.remote.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.mitashi.ht4440bt.remote.data.IrButtonCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Hardware abstraction layer over Android's native ConsumerIrManager.
 * Handles hardware capability detection, carrier frequency verification,
 * asynchronous IR pulse transmission, and crisp tactile haptic feedback.
 */
class IrBlasterController(context: Context) {

    private val appContext = context.applicationContext
    private val irManager: ConsumerIrManager? =
        appContext.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    val hasIrEmitter: Boolean
        get() = try {
            irManager?.hasIrEmitter() == true
        } catch (_: Throwable) {
            false
        }

    fun getSupportedFrequencySummary(): String {
        val manager = irManager ?: return "No IR hardware detected"
        if (!hasIrEmitter) return "No built-in IR blaster detected"
        return try {
            val ranges = manager.carrierFrequencies
            if (ranges.isNullOrEmpty()) {
                "IR Blaster Ready (38 kHz)"
            } else {
                val minKhz = ranges.minOf { it.minFrequency } / 1000
                val maxKhz = ranges.maxOf { it.maxFrequency } / 1000
                "IR Blaster Ready ($minKhz–$maxKhz kHz)"
            }
        } catch (_: Throwable) {
            "IR Blaster Ready"
        }
    }

    suspend fun transmit(code: IrButtonCode, vibrate: Boolean = true): Result<IrProtocolEncoder.EncodedSignal> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val encoded = IrProtocolEncoder.encode(code)
                if (vibrate) triggerHaptic()
                val manager = irManager
                if (manager != null && hasIrEmitter) {
                    manager.transmit(encoded.frequencyHz, encoded.patternUs)
                }
                encoded
            }
        }
    }

    suspend fun transmitNecRepeat(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val repeat = IrProtocolEncoder.encodeNecRepeat()
                val manager = irManager
                if (manager != null && hasIrEmitter) {
                    manager.transmit(repeat.frequencyHz, repeat.patternUs)
                }
            }
        }
    }

    private fun triggerHaptic() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18L)
            }
        } catch (_: Throwable) {
            // Ignore haptic errors on devices without vibrator permission/hardware
        }
    }
}
