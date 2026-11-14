# Void Vine Alert

RuneLite plugin that plays a sound when someone gets rooted by Ocellus' Void Vine (phase 3, The Fractured Archive).

## What you need
- Java JDK 11 or newer (e.g. from adoptium.net)
- Internet the first time you run it (it downloads RuneLite)

## Running it
**Easiest: IntelliJ IDEA (free Community edition)**
1. File > Open > pick the `void-vine-alert` folder. Let it finish loading Gradle.
2. Open `src/test/java/com/vinealert/VineAlertPluginTest.java` and click the green play button next to `main`.
3. In VM options add `-ea` if it asks (Run > Edit Configurations), and in Program arguments add `--developer-mode`.

**Or from a terminal** in the `void-vine-alert` folder:
- Windows: `gradlew.bat run`
- Mac/Linux: `./gradlew run`

RuneLite opens with the plugin loaded. Log in, then turn on **Void Vine Alert** in the plugin list.

## The sound
A placeholder beep is built in (`src/main/resources/com/vinealert/vine.wav`), so you hear something straight away.
To use your own sound:
- Replace that `vine.wav` with your file (keep the name, must be **.wav**), or
- Paste the full path of a .wav into **Custom sound file** in the plugin settings. No restart needed.

## Settings
- **Only when I'm rooted**: ignore teammates' vines.
- **Early warning on shout**: also play when Ocellus shouts "The void... will take you..." (~3 ticks earlier).
- **Volume**: 0-100.
- **Play test sound**: tick to hear the alert once. You can also type `::vinetest` in the chatbox.

## If it doesn't go off
Turn on **Early warning on shout**. That uses the boss's shout instead of the vine itself, so it still works if the vine detection misses.
