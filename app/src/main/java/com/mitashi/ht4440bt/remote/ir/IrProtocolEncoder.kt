package com.mitashi.ht4440bt.remote.ir

import com.mitashi.ht4440bt.remote.data.IrButtonCode
import com.mitashi.ht4440bt.remote.data.IrProtocolType
import kotlin.math.roundToInt

/**
 * Pure-Kotlin IR pulse-train encoder generating microsecond mark/space arrays
 * directly compatible with Android's ConsumerIrManager.transmit(carrierFrequency, pattern).
 */
object IrProtocolEncoder {

    const val NEC_CARRIER_HZ = 38_000
    private const val NEC_HDR_MARK = 9000
    private const val NEC_HDR_SPACE = 4500
    private const val NEC_BIT_MARK = 562
    private const val NEC_ONE_SPACE = 1688
    private const val NEC_ZERO_SPACE = 562
    private const val NEC_TAIL_SPACE = 40_000

    data class EncodedSignal(
        val frequencyHz: Int,
        val patternUs: IntArray
    )

    /**
     * Encodes an [IrButtonCode] into carrier frequency (Hz) + alternating mark/space durations (us).
     */
    fun encode(code: IrButtonCode): EncodedSignal {
        return when (code.protocol) {
            IrProtocolType.NEC -> EncodedSignal(
                frequencyHz = code.frequencyHz.coerceIn(30_000, 56_000),
                patternUs = encodeNecStandard(code.address, code.command)
            )
            IrProtocolType.NEC_EXT -> EncodedSignal(
                frequencyHz = code.frequencyHz.coerceIn(30_000, 56_000),
                patternUs = encodeNecExtended(code.address, code.command)
            )
            IrProtocolType.PRONTO_HEX -> encodeProntoHex(code.prontoHex)
            IrProtocolType.RAW_US -> {
                require(code.rawPatternUs.isNotEmpty()) { "Raw IR pattern cannot be empty." }
                val normalized = if (code.rawPatternUs.size % 2 == 1) {
                    code.rawPatternUs + NEC_TAIL_SPACE
                } else {
                    code.rawPatternUs
                }
                EncodedSignal(
                    frequencyHz = code.frequencyHz.coerceIn(20_000, 60_000),
                    patternUs = normalized
                )
            }
        }
    }

    /**
     * Standard 32-bit NEC frame (LSB-first per byte):
     * [address, ~address, command, ~command]
     */
    fun encodeNecStandard(address: Int, command: Int): IntArray {
        val addr = address and 0xFF
        val addrInv = addr.inv() and 0xFF
        val cmd = command and 0xFF
        val cmdInv = cmd.inv() and 0xFF
        return buildNecFrame(addr, addrInv, cmd, cmdInv)
    }

    /**
     * Extended 32-bit NEC frame (16-bit address, LSB-first):
     * [addrLow, addrHigh, command, ~command]
     */
    fun encodeNecExtended(address16: Int, command: Int): IntArray {
        val addrLow = address16 and 0xFF
        val addrHigh = (address16 shr 8) and 0xFF
        val cmd = command and 0xFF
        val cmdInv = cmd.inv() and 0xFF
        return buildNecFrame(addrLow, addrHigh, cmd, cmdInv)
    }

    /**
     * NEC Repeat frame transmitted every ~110ms while holding a rocker button (VOL+, VOL-, SW+, SW-).
     */
    fun encodeNecRepeat(): EncodedSignal = EncodedSignal(
        frequencyHz = NEC_CARRIER_HZ,
        patternUs = intArrayOf(NEC_HDR_MARK, 2250, NEC_BIT_MARK, 96_188)
    )

    private fun buildNecFrame(b0: Int, b1: Int, b2: Int, b3: Int): IntArray {
        val pulses = IntArray(2 + 32 * 2 + 2)
        var idx = 0
        pulses[idx++] = NEC_HDR_MARK
        pulses[idx++] = NEC_HDR_SPACE

        val bytes = intArrayOf(b0, b1, b2, b3)
        for (byteVal in bytes) {
            for (bit in 0 until 8) {
                pulses[idx++] = NEC_BIT_MARK
                val isOne = ((byteVal shr bit) and 1) == 1
                pulses[idx++] = if (isOne) NEC_ONE_SPACE else NEC_ZERO_SPACE
            }
        }
        pulses[idx++] = NEC_BIT_MARK
        pulses[idx] = NEC_TAIL_SPACE
        return pulses
    }

    /**
     * Parses a standard Pronto CCF Hex string (starting with 0000) into frequency + microsecond timings.
     */
    fun encodeProntoHex(prontoHex: String): EncodedSignal {
        val tokens = prontoHex
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }

        require(tokens.size >= 6) { "Pronto hex string is too short." }
        val words = tokens.map { it.toInt(16) }
        require(words[0] == 0x0000) { "Only learned Pronto format (0000) is supported." }

        val freqCode = words[1]
        require(freqCode > 0) { "Invalid Pronto carrier frequency word." }

        val frequencyHz = (1_000_000.0 / (freqCode * 0.241246)).roundToInt()
        val periodUs = 1_000_000.0 / frequencyHz

        val burstWords = words.drop(4)
        val rawUs = IntArray(burstWords.size) { i ->
            (burstWords[i] * periodUs).roundToInt().coerceAtLeast(100)
        }
        val normalized = if (rawUs.size % 2 == 1) rawUs + NEC_TAIL_SPACE else rawUs
        return EncodedSignal(frequencyHz = frequencyHz, patternUs = normalized)
    }

    /**
     * Parses Flipper Zero .ir file text into a map of button name -> [IrButtonCode].
     */
    fun parseFlipperIrFile(content: String): Map<String, IrButtonCode> {
        val result = linkedMapOf<String, IrButtonCode>()
        var currentName: String? = null
        var currentProtocol: String? = null
        var currentAddress: Int? = null
        var currentCommand: Int? = null
        var currentFreq = NEC_CARRIER_HZ
        var currentRaw: IntArray? = null

        fun flush() {
            val name = currentName ?: return
            if (currentRaw != null) {
                result[name] = IrButtonCode(
                    protocol = IrProtocolType.RAW_US,
                    frequencyHz = currentFreq,
                    rawPatternUs = currentRaw!!
                )
            } else if (currentProtocol != null && currentAddress != null && currentCommand != null) {
                val proto = if (currentProtocol.equals("NECext", ignoreCase = true)) {
                    IrProtocolType.NEC_EXT
                } else {
                    IrProtocolType.NEC
                }
                result[name] = IrButtonCode(
                    protocol = proto,
                    frequencyHz = NEC_CARRIER_HZ,
                    address = currentAddress!!,
                    command = currentCommand!!
                )
            }
            currentName = null
            currentProtocol = null
            currentAddress = null
            currentCommand = null
            currentFreq = NEC_CARRIER_HZ
            currentRaw = null
        }

        for (rawLine in content.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) {
                if (currentName != null) flush()
                continue
            }
            val parts = line.split(":", limit = 2)
            if (parts.size != 2) continue
            val key = parts[0].trim().lowercase()
            val value = parts[1].trim()
            when (key) {
                "name" -> {
                    if (currentName != null) flush()
                    currentName = value
                }
                "protocol" -> currentProtocol = value
                "address" -> {
                    val bytes = value.split(Regex("\\s+")).mapNotNull { it.toIntOrNull(16) }
                    if (bytes.size >= 2) {
                        currentAddress = (bytes[1] shl 8) or bytes[0]
                    } else if (bytes.size == 1) {
                        currentAddress = bytes[0]
                    }
                }
                "command" -> {
                    val bytes = value.split(Regex("\\s+")).mapNotNull { it.toIntOrNull(16) }
                    if (bytes.isNotEmpty()) currentCommand = bytes[0]
                }
                "frequency" -> currentFreq = value.toIntOrNull() ?: NEC_CARRIER_HZ
                "data" -> {
                    currentRaw = value.split(Regex("\\s+")).mapNotNull { it.toIntOrNull() }.toIntArray()
                }
            }
        }
        flush()
        return result
    }
}
