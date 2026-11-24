package com.mitashi.ht4440bt.remote

import com.mitashi.ht4440bt.remote.data.IrButtonCode
import com.mitashi.ht4440bt.remote.data.IrProtocolType
import com.mitashi.ht4440bt.remote.data.PresetProfiles
import com.mitashi.ht4440bt.remote.data.RemoteButton
import com.mitashi.ht4440bt.remote.ir.IrProtocolEncoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IrProtocolEncoderTest {

    @Test
    fun necStandardFrame_hasExact68PulsesAndValidHeader() {
        val pattern = IrProtocolEncoder.encodeNecStandard(address = 0x00, command = 0x45)
        // 2 header + 64 data (32 bits * 2) + 2 stop/tail = 68 integers
        assertEquals(68, pattern.size)
        assertEquals(9000, pattern[0])
        assertEquals(4500, pattern[1])
        assertEquals(562, pattern[66])
    }

    @Test
    fun all32ButtonsPresentInEveryPreset() {
        assertEquals(32, RemoteButton.entries.size)
        for (preset in PresetProfiles.allPresets) {
            assertEquals(
                "Preset ${preset.name} must map all 32 buttons",
                32,
                preset.buttonCodes.size
            )
        }
    }

    @Test
    fun flipperIrParser_readsNecBlock() {
        val sample = """
            Filetype: IR signals file
            Version: 1
            #
            name: POWER
            type: parsed
            protocol: NEC
            address: 00 00 00 00
            command: 45 00 00 00
        """.trimIndent()
        val parsed = IrProtocolEncoder.parseFlipperIrFile(sample)
        assertTrue(parsed.containsKey("POWER"))
        val code = parsed["POWER"]!!
        assertEquals(IrProtocolType.NEC, code.protocol)
        assertEquals(0x00, code.address)
        assertEquals(0x45, code.command)
    }

    @Test
    fun encodeCode_returns38kHzCarrierForNec() {
        val signal = IrProtocolEncoder.encode(
            IrButtonCode(protocol = IrProtocolType.NEC, address = 0x80, command = 0x12)
        )
        assertEquals(38_000, signal.frequencyHz)
        assertEquals(68, signal.patternUs.size)
    }
}
