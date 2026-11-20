# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

PromptPay QR+ — a small single-module Android app (Java, package `com.diewland.android.qr_pp_plus`, published on Google Play) that generates Thai PromptPay payment QR codes, optionally with an amount and a text remark, and shares them as PNG images.

## Build & test

Use the Gradle wrapper from the repo root (`gradlew` is not executable in git, so call it via `sh`; Gradle needs JDK 17+ as `JAVA_HOME`):

```bash
sh ./gradlew assembleDebug          # build debug APK
sh ./gradlew assembleRelease        # build release APK (minify disabled)
sh ./gradlew installDebug           # install on connected device/emulator
sh ./gradlew test                   # JVM unit tests (app/src/test)
sh ./gradlew connectedAndroidTest   # instrumented tests (needs device)
sh ./gradlew testDebugUnitTest --tests com.diewland.android.qr_pp_plus.PromptPayTest   # single test class (needs a variant task, not `test`)
```

There is no linter configured beyond `sh ./gradlew lint`. Unit tests cover `PromptPay` (golden payload strings — any payload change must keep these passing or be deliberate), `CRC16` and `Calc`; there are no UI tests.

Toolchain:
- Gradle wrapper 8.14.3, Android Gradle Plugin 8.13.2.
- `compileSdk 36`, `targetSdkVersion 36` (Google Play requires targeting Android 16), `minSdkVersion 21` (AndroidX floor).
- AndroidX (`android.useAndroidX=true`); no Support Library left. QR rendering comes from `com.github.kenglxn.QRGen:android` via JitPack.
- Version is bumped manually in `app/build.gradle` (`versionCode` / `versionName`); Play rejects a reused `versionCode`.
- A debug build can't install over the Play Store copy (different signing key). Installing it on a phone means uninstalling the Play app, which wipes the user's saved inputs — ask first.

## Architecture

All code lives in `app/src/main/java/com/diewland/android/qr_pp_plus/`:

- **`PromptPay`** — pure Java: `AccountType.of(id)` and `payload(id, amount)` (null for an invalid ID). Keep payload logic here, not in activities.
- **`MainActivity`** — UI for the QR flow. Every text change calls `renderQR()`, which gets the payload from `PromptPay`, renders a 512×512 bitmap with QRGen (cached per payload, so typing a remark doesn't re-encode), and optionally draws the remark centered in the QR's top quiet zone. Remark size is relative to the bitmap, not screen density, so shared PNGs look the same on every device. The calculator opens from the amount field's end icon. Inputs are persisted to `SharedPreferences("SAVE_STATE")` in `onPause` and restored in `onCreate`.
- **`CRC16`** — CRC-16/CCITT-FALSE (init `0xFFFF`, poly `0x1021`) returning 4 uppercase hex chars; required as the payload's final field (`6304` + checksum).
- **`CalcActivity`** + **`Calc`** — a simple calculator launched via `registerForActivityResult`. The amount is passed in and returned through `CalcActivity.EXTRA_AMOUNT`. The `=` button turns into `OK` after a result is computed; pressing `OK` returns the value. `Calc.ops` formats results as integer, 1-decimal, or 2-decimal strings. Digit and operator keys (`TEXT_KEYS`) are dispatched by their button **text** (`"x"`, `"÷"`…) — keep layout labels and `Calc` operator strings in sync. Delete and `=`/`OK` have their own listeners (`okMode` tracks which the equal key shows).
- **`EdgeToEdgeHelper`** — called by both activities right after `setContentView()`. Targeting API 35+ forces edge-to-edge and the system no longer paints bar colors, so it makes the bars transparent with light icons over a `colorPrimaryDark` window background, and applies system-bar/cutout/IME insets as **margins** on the layout root. Activities use `adjustResize` so the IME inset also works on old API levels (`CalcActivity` also uses `stateAlwaysHidden` + `setShowSoftInputOnFocus(false)` because its keypad is the input). New activities must call it too.

UI is Material 3 (`Theme.Material3.Light.NoActionBar`; colors, key and field styles in `res/values`). Both screens use a `MaterialToolbar` set as the support action bar.

### PromptPay payload rules (in `PromptPay`)

Account ID type is inferred purely from input length:
- 10 digits → phone number: `01130066` + number without leading `0`
- 13 digits → national ID: `0213` + id
- 15 digits → TrueMoney e-wallet: `0315` + id
- anything else → `payload()` returns null (UI shows the placeholder and disables share)

Payload = `000201010211` + tag `29` (merchant info, AID `A000000677010111` + account) + `5303764` (THB) + optional tag `54` amount + `5802TH` + `6304` + CRC16. Format numbers in the payload with `Locale.ROOT`, never the device locale (some locales emit non-ASCII digits). References for the format are listed in `README.md` (based on dtinth/promptpay-qr).

### Sharing

The share button clears `cacheDir/images/`, writes the bitmap to `pp-<uuid>.png` there and shares it via `ShareCompat` through a `FileProvider` (authority `${applicationId}.fileprovider` in the manifest, `getPackageName() + ".fileprovider"` in code; paths in `res/xml/filepaths.xml`), so no storage permission is needed.
