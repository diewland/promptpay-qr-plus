# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

PromptPay QR+ — a small single-module Android app (Java, package `com.diewland.android.qr_pp_plus`, published on Google Play) that generates Thai PromptPay payment QR codes, optionally with an amount and a text remark, and shares them as PNG images.

## Build & test

Use the Gradle wrapper from the repo root:

```bash
./gradlew assembleDebug          # build debug APK
./gradlew assembleRelease        # build release APK (minify disabled)
./gradlew installDebug           # install on connected device/emulator
./gradlew test                   # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest   # instrumented tests (needs device)
./gradlew test --tests com.diewland.android.qr_pp_plus.ExampleUnitTest   # single test class
```

There is no linter configured beyond `./gradlew lint`. Existing tests are only the Android Studio template stubs.

Toolchain:
- Gradle wrapper 8.14.3, Android Gradle Plugin 8.13.2 — Gradle must run on JDK 17+ (e.g. `JAVA_HOME=<jdk17> sh ./gradlew ...`; `gradlew` is not marked executable in git).
- `compileSdk 36`, `targetSdkVersion 36` (Google Play requires targeting Android 16), `minSdkVersion 21` (AndroidX floor).
- AndroidX (`android.useAndroidX=true`); no Support Library left. QR rendering comes from `com.github.kenglxn.QRGen:android` via JitPack.
- Version is bumped manually in `app/build.gradle` (`versionCode` / `versionName`).

## Architecture

All code lives in `app/src/main/java/com/diewland/android/qr_pp_plus/`:

- **`MainActivity`** — the whole QR flow. Every text change on account ID / amount / remark calls `renderQR()`, which builds the PromptPay EMVCo payload string by hand, appends the CRC, renders a 512×512 bitmap with QRGen, and optionally draws the remark text onto the bitmap. Inputs are persisted to `SharedPreferences("SAVE_STATE")` in `onPause` and restored in `onCreate`.
- **`CRC16`** — CRC-16/CCITT-FALSE (init `0xFFFF`, poly `0x1021`) returning 4 uppercase hex chars; required as the payload's final field (`6304` + checksum).
- **`CalcActivity`** + **`Calc`** — a simple calculator launched via `startActivityForResult` (`PICK_AMOUNT`). The amount is passed in and returned through the `"AMOUNT"` intent extra. The `=` button turns into `OK` after a result is computed; pressing `OK` returns the value. `Calc.ops` formats results as integer, 1-decimal, or 2-decimal strings.

- **`EdgeToEdgeHelper`** — called by both activities right after `setContentView()`. Targeting API 35+ forces edge-to-edge and the system no longer paints bar colors, so it makes the bars transparent with light icons over a `colorPrimaryDark` window background, and applies system-bar/cutout/IME insets as **margins** on the layout root. Activities use `adjustResize` so the IME inset also works on old API levels. New activities must call it too.

### PromptPay payload rules (in `MainActivity.renderQR`)

Account ID type is inferred purely from input length:
- 10 digits → phone number: `01130066` + number without leading `0`
- 13 digits → national ID: `0213` + id
- 15 digits → TrueMoney e-wallet: `0315` + id
- anything else → no QR, share button hidden

Payload = `000201010211` + tag `29` (merchant info, AID `A000000677010111` + account) + `5303764` (THB) + optional tag `54` amount + `5802TH` + `6304` + CRC16. References for the format are listed in `README.md` (based on dtinth/promptpay-qr).

### Sharing

The share button writes the bitmap to `cacheDir/images/pp-<uuid>.png` and shares it through a `FileProvider` (authority `com.diewland.android.qr_pp_plus.fileprovider`, paths in `res/xml/filepaths.xml`), so no storage permission is needed. Keep the authority string in `MainActivity` and `AndroidManifest.xml` in sync.
