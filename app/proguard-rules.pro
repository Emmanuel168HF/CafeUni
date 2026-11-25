# Reglas de ProGuard / R8 para Café Uni (Play Store Release)

# Room Database
-keep class * extends androidx.room.RoomDatabase

# ZXing QR Generator
-keep class com.google.zxing.qrcode.** { *; }

# Coil Image Loader
-keep class coil.** { *; }
