# PromptPay QR+

<a href='https://play.google.com/store/apps/details?id=com.diewland.android.qr_pp_plus' title='Now available on Google Play'><img width='300' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'></a>

Android app that makes Thai PromptPay QR codes for a phone number, national ID or e-Wallet ID, with an optional amount (built-in calculator) and a remark printed on the image, then shares the QR as a PNG.

### Build
Requires JDK 17+ and the Android SDK (API 36). Targets Android 16; runs on Android 5.0+.

```bash
sh ./gradlew assembleDebug   # build APK
sh ./gradlew test            # unit tests (payload, CRC, calculator)
```

### References
* https://www.blognone.com/node/95133
* https://www.blognone.com/node/95311
* https://www.blognone.com/node/95530
* https://github.com/dtinth/promptpay-qr
* http://introcs.cs.princeton.edu/java/61data/CRC16CCITT.java
* http://www.truemoney.com/promptpay-aom/
* https://www.blognone.com/node/97186
