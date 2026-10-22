# drumPads Android project

Native Android app built with Kotlin and Jetpack Compose.

## Requirements
- JDK 17
- Android SDK Platform 35 and Android Build Tools (installed via Android Studio or Android SDK command-line tools)
- Gradle 8.7

## Build on Windows
Open PowerShell in this folder and run:

```powershell
gradle assembleDebug
```

This project archive does not include the Gradle Wrapper (`gradlew.bat` and `gradle-wrapper.jar`). If Gradle is installed and available on PATH, the command above builds the APK. Otherwise, open the project in Android Studio and let it configure the Gradle environment, or install Gradle 8.7 and the Android SDK.

APK output:
`app\build\outputs\apk\debug\app-debug.apk`

## Notes
- `compileSdk` and `targetSdk` are 35.
- Java/Kotlin target is 17.
- Gradle memory use is limited to help computers with 4 GB RAM.
