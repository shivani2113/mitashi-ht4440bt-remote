package com.mitashi.ht4440bt.remote.data

/**
 * Curated NEC 38 kHz profiles covering the OEM multimedia 4.1/5.1 home theatre boards
 * commonly used in Mitashi, Zebronics, Intex, Conic, and F&D systems.
 *
 * Because Mitashi never published an official hex table for HT4440BT, users can switch
 * between these 6 common OEM NEC addresses with one tap or use the built-in IR Scanner
 * to lock onto their exact hardware address & fine-tune any individual button.
 */
object PresetProfiles {

    private fun buildProfile(
        id: String,
        name: String,
        subtitle: String,
        address: Int,
        protocol: IrProtocolType = IrProtocolType.NEC,
        cmdMap: Map<RemoteButton, Int>
    ): RemoteProfile {
        val codes = RemoteButton.entries.associateWith { button ->
            IrButtonCode(
                protocol = protocol,
                frequencyHz = 38_000,
                address = address,
                command = cmdMap[button] ?: 0x00
            )
        }
        return RemoteProfile(
            id = id,
            name = name,
            subtitle = subtitle,
            defaultAddress = address,
            protocol = protocol,
            buttonCodes = codes
        )
    }

    // Standard HT6221 / uPD6121 4.1 Bluetooth Home Theatre command layout (Address 0x00)
    private val standardHtCommands: Map<RemoteButton, Int> = mapOf(
        RemoteButton.POWER to 0x45,
        RemoteButton.INPUT to 0x46,
        RemoteButton.SAVE to 0x44,
        RemoteButton.SLEEP to 0x40,
        RemoteButton.MUTE to 0x47,
        RemoteButton.EQ_MINUS to 0x07,
        RemoteButton.EQ_PLUS to 0x09,
        RemoteButton.TUNE_UP to 0x18,
        RemoteButton.CH_PREV to 0x08,
        RemoteButton.PLAY_PAUSE_AUTO to 0x1C,
        RemoteButton.CH_NEXT to 0x5A,
        RemoteButton.TUNE_DOWN to 0x52,
        RemoteButton.DISP_MINUS to 0x15,
        RemoteButton.DISP_PLUS to 0x19,
        RemoteButton.SW_UP to 0x0C,
        RemoteButton.SW_DOWN to 0x42,
        RemoteButton.TRE_UP to 0x5E,
        RemoteButton.TRE_DOWN to 0x4A,
        RemoteButton.VOL_UP to 0x15,
        RemoteButton.VOL_DOWN to 0x07,
        RemoteButton.NUM_1 to 0x0C,
        RemoteButton.NUM_2 to 0x18,
        RemoteButton.NUM_3 to 0x5E,
        RemoteButton.NUM_4 to 0x08,
        RemoteButton.NUM_5 to 0x1C,
        RemoteButton.NUM_6 to 0x5A,
        RemoteButton.NUM_7 to 0x42,
        RemoteButton.NUM_8 to 0x52,
        RemoteButton.NUM_9 to 0x4A,
        RemoteButton.REPEAT to 0x0D,
        RemoteButton.NUM_0 to 0x16,
        RemoteButton.RESET to 0x19
    )

    // Alternate PT2322 / SC7313 4.1 Home Theatre board layout (Address 0x80 / 0x01 / 0x40)
    private val altHtCommands: Map<RemoteButton, Int> = mapOf(
        RemoteButton.POWER to 0x12,
        RemoteButton.INPUT to 0x1A,
        RemoteButton.SAVE to 0x1E,
        RemoteButton.SLEEP to 0x0F,
        RemoteButton.MUTE to 0x10,
        RemoteButton.EQ_MINUS to 0x14,
        RemoteButton.EQ_PLUS to 0x15,
        RemoteButton.TUNE_UP to 0x02,
        RemoteButton.CH_PREV to 0x04,
        RemoteButton.PLAY_PAUSE_AUTO to 0x05,
        RemoteButton.CH_NEXT to 0x06,
        RemoteButton.TUNE_DOWN to 0x08,
        RemoteButton.DISP_MINUS to 0x16,
        RemoteButton.DISP_PLUS to 0x17,
        RemoteButton.SW_UP to 0x1B,
        RemoteButton.SW_DOWN to 0x1F,
        RemoteButton.TRE_UP to 0x1C,
        RemoteButton.TRE_DOWN to 0x1D,
        RemoteButton.VOL_UP to 0x0A,
        RemoteButton.VOL_DOWN to 0x0B,
        RemoteButton.NUM_1 to 0x01,
        RemoteButton.NUM_2 to 0x02,
        RemoteButton.NUM_3 to 0x03,
        RemoteButton.NUM_4 to 0x04,
        RemoteButton.NUM_5 to 0x05,
        RemoteButton.NUM_6 to 0x06,
        RemoteButton.NUM_7 to 0x07,
        RemoteButton.NUM_8 to 0x08,
        RemoteButton.NUM_9 to 0x09,
        RemoteButton.REPEAT to 0x0C,
        RemoteButton.NUM_0 to 0x00,
        RemoteButton.RESET to 0x0E
    )

    val allPresets: List<RemoteProfile> = listOf(
        buildProfile(
            id = "mitashi_nec_00",
            name = "Preset 1 — Mitashi / OEM 0x00",
            subtitle = "Standard NEC 38kHz (Addr 0x00)",
            address = 0x00,
            cmdMap = standardHtCommands
        ),
        buildProfile(
            id = "mitashi_nec_80",
            name = "Preset 2 — Mitashi / Zebronics 0x80",
            subtitle = "NEC 38kHz (Addr 0x80)",
            address = 0x80,
            cmdMap = altHtCommands
        ),
        buildProfile(
            id = "mitashi_nec_01",
            name = "Preset 3 — 4.1 Audio Board 0x01",
            subtitle = "NEC 38kHz (Addr 0x01)",
            address = 0x01,
            cmdMap = altHtCommands
        ),
        buildProfile(
            id = "mitashi_nec_40",
            name = "Preset 4 — Multimedia HT 0x40",
            subtitle = "NEC 38kHz (Addr 0x40)",
            address = 0x40,
            cmdMap = altHtCommands
        ),
        buildProfile(
            id = "mitashi_nec_ef",
            name = "Preset 5 — Extended NEC 0xEF00",
            subtitle = "NEC-Ext 38kHz (Addr 0xEF00)",
            address = 0xEF00,
            protocol = IrProtocolType.NEC_EXT,
            cmdMap = standardHtCommands
        ),
        buildProfile(
            id = "mitashi_nec_fd",
            name = "Preset 6 — Audio OEM 0xFD",
            subtitle = "NEC 38kHz (Addr 0xFD)",
            address = 0xFD,
            cmdMap = standardHtCommands
        )
    )
}
