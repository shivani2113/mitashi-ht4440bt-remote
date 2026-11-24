# Mitashi HT4440BT — Android IR Remote App

A native Android (Kotlin + Jetpack Compose) infrared remote control built specifically for the **Mitashi HT4440BT** 4.1-channel Bluetooth home theatre system.

## Features

- **1:1 Physical Remote Layout (All 32 Buttons):** Power, INPUT, SAVE, SLEEP, MUTE, EQ (-/+), circular D-Pad (TUNE+, CH-, ▶❚❚ AUTO, CH+, TUNE-), DISP (-/+), SW (+/-) rocker, TRE+/TRE-, VOL (+/-) rocker, 0–9 keypad, REPEAT, and RESET.
- **Native Android IR Blaster Engine:** Uses `android.hardware.ConsumerIrManager` (`TRANSMIT_IR`) with a pure-Kotlin **38 kHz NEC**, **Extended NEC**, **Pronto Hex**, and **Raw Microsecond** pulse encoder.
- **6 Built-in OEM Presets + Live NEC IR Scanner:** Because Mitashi never published an official hex table for the HT4440BT, the app includes 6 common Mitashi/Zebronics/Intex NEC presets and a built-in **NEC Scanner** (`0x00`–`0xFF`) so you can lock onto your exact hardware address in seconds and save every button permanently.
- **Long-Press Per-Button Editor + Backup/Import:** Long-press any button to edit its hex code, export your calibrated JSON profile, or import a Flipper Zero `.ir` file.
- **Dark & Light Themes + Haptic Feedback:** Dark mode by default for movie watching, with a live IR transmission LED indicator.

## How to Build the Installable `.apk`

### Option A: Build with Android Studio (Local)
1. Unzip this folder and open it in **Android Studio** (Ladybug or newer).
2. Let Gradle sync (JDK 17, Android SDK 35).
3. Connect your Android phone with USB Debugging enabled and click **Run** (or choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**).

### Option B: Build Automatically via GitHub Actions (No Android Studio Needed)
1. Create a new GitHub repository and push the contents of this folder to `main`.
2. Open the **Actions** tab on GitHub — the included `.github/workflows/build-apk.yml` workflow will automatically run unit tests and compile `app-debug.apk`.
3. Download the `Mitashi-HT4440BT-Remote-debug-apk` artifact, transfer the APK to your phone, and install it.

## Calibrating Your Mitashi HT4440BT
1. Point the top IR blaster of your phone directly at the Mitashi HT4440BT subwoofer sensor.
2. Tap the **Remote icon** (top-right) to test **Presets 1–6** with the **Power** or **Mute** button.
3. If none of the presets match immediately, tap the **Tune/Scanner icon**, run **Auto-Scan** (or step through NEC addresses), and tap **Apply Address to All Buttons** as soon as your home theatre responds.
