package com.mitashi.ht4440bt.remote.data

/**
 * Supported IR encoding types for Mitashi HT4440BT and compatible OEM home theatre boards.
 */
enum class IrProtocolType {
    /** Standard 32-bit NEC (8-bit address + inverted address + 8-bit command + inverted command) */
    NEC,
    /** Extended 32-bit NEC (16-bit address + 8-bit command + inverted command) */
    NEC_EXT,
    /** Pronto CCF hex string (e.g. "0000 006D 0022 0002 ...") */
    PRONTO_HEX,
    /** Raw comma-separated microsecond mark/space durations */
    RAW_US
}

data class IrButtonCode(
    val protocol: IrProtocolType = IrProtocolType.NEC,
    val frequencyHz: Int = 38_000,
    /** For NEC: 0x00..0xFF; for NEC_EXT: 0x0000..0xFFFF */
    val address: Int = 0x00,
    /** For NEC / NEC_EXT: 0x00..0xFF */
    val command: Int = 0x00,
    /** Used when protocol is PRONTO_HEX */
    val prontoHex: String = "",
    /** Used when protocol is RAW_US */
    val rawPatternUs: IntArray = intArrayOf()
) {
    fun displaySummary(): String = when (protocol) {
        IrProtocolType.NEC -> "NEC 0x%02X : 0x%02X".format(address and 0xFF, command and 0xFF)
        IrProtocolType.NEC_EXT -> "NEC-Ext 0x%04X : 0x%02X".format(address and 0xFFFF, command and 0xFF)
        IrProtocolType.PRONTO_HEX -> "Pronto (${prontoHex.take(14)}…)"
        IrProtocolType.RAW_US -> "Raw (${rawPatternUs.size} pulses @ ${frequencyHz / 1000}kHz)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IrButtonCode) return false
        return protocol == other.protocol &&
            frequencyHz == other.frequencyHz &&
            address == other.address &&
            command == other.command &&
            prontoHex == other.prontoHex &&
            rawPatternUs.contentEquals(other.rawPatternUs)
    }

    override fun hashCode(): Int {
        var result = protocol.hashCode()
        result = 31 * result + frequencyHz
        result = 31 * result + address
        result = 31 * result + command
        result = 31 * result + prontoHex.hashCode()
        result = 31 * result + rawPatternUs.contentHashCode()
        return result
    }
}

data class RemoteProfile(
    val id: String,
    val name: String,
    val subtitle: String,
    val defaultAddress: Int,
    val protocol: IrProtocolType = IrProtocolType.NEC,
    val buttonCodes: Map<RemoteButton, IrButtonCode>
)
