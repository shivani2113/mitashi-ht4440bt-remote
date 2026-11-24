package com.mitashi.ht4440bt.remote.data

/**
 * All 32 physical buttons mapped directly from the Mitashi HT4440BT remote control.
 */
enum class RemoteButton(
    val id: String,
    val label: String,
    val description: String,
    val supportsHoldRepeat: Boolean = false
) {
    POWER("POWER", "POWER", "Standby / Power toggle"),
    INPUT("INPUT", "INPUT", "Switch source (BT / AUX / USB / FM)"),
    SAVE("SAVE", "SAVE", "Save FM radio preset"),
    SLEEP("SLEEP", "SLEEP", "Sleep timer"),
    MUTE("MUTE", "MUTE", "Mute / Unmute audio"),

    EQ_MINUS("EQ_MINUS", "EQ -", "Previous equalizer preset"),
    EQ_PLUS("EQ_PLUS", "EQ +", "Next equalizer preset"),
    TUNE_UP("TUNE_UP", "TUNE+", "FM frequency tune up", true),
    CH_PREV("CH_PREV", "CH-", "Previous track / FM channel"),
    PLAY_PAUSE_AUTO("PLAY_PAUSE_AUTO", "AUTO", "Play / Pause / FM Auto-Scan"),
    CH_NEXT("CH_NEXT", "CH+", "Next track / FM channel"),
    TUNE_DOWN("TUNE_DOWN", "TUNE-", "FM frequency tune down", true),
    DISP_MINUS("DISP_MINUS", "DISP -", "Display brightness down"),
    DISP_PLUS("DISP_PLUS", "DISP +", "Display brightness up"),

    SW_UP("SW_UP", "SW +", "Subwoofer bass up", true),
    SW_DOWN("SW_DOWN", "SW -", "Subwoofer bass down", true),
    TRE_UP("TRE_UP", "TRE+", "Treble up", true),
    TRE_DOWN("TRE_DOWN", "TRE-", "Treble down", true),
    VOL_UP("VOL_UP", "VOL +", "Master volume up", true),
    VOL_DOWN("VOL_DOWN", "VOL -", "Master volume down", true),

    NUM_1("NUM_1", "1", "Digit 1"),
    NUM_2("NUM_2", "2", "Digit 2"),
    NUM_3("NUM_3", "3", "Digit 3"),
    NUM_4("NUM_4", "4", "Digit 4"),
    NUM_5("NUM_5", "5", "Digit 5"),
    NUM_6("NUM_6", "6", "Digit 6"),
    NUM_7("NUM_7", "7", "Digit 7"),
    NUM_8("NUM_8", "8", "Digit 8"),
    NUM_9("NUM_9", "9", "Digit 9"),
    REPEAT("REPEAT", "REPEAT", "Repeat track / folder"),
    NUM_0("NUM_0", "0", "Digit 0"),
    RESET("RESET", "RESET", "Reset audio settings to factory defaults")
}
