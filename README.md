# PocketNote

Aplikasi catatan offline Android native (Java), tanpa API key.

## Build lokal
Memerlukan JDK 17 dan Gradle 8.9:
```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Build dengan GitHub Actions
1. Push project ke GitHub.
2. Buka tab **Actions**.
3. Pilih **Build PocketNote APK**.
4. Jalankan workflow jika belum otomatis berjalan.
5. Buka hasil run dan unduh artifact **PocketNote-debug**.

Package: `com.rafi.pocketnote`
Minimum Android: 6.0 (API 23)
Target Android: 15 (API 35)
