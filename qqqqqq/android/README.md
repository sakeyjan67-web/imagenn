# CamTok for Android

Native Android app project using Kotlin, Jetpack Compose, Coil, and AndroidX Media3. The browser prototype remains at the workspace root; this project lives in `android/`.

## Open and run

Open this `android` folder in Android Studio. Install Android Studio, JDK 17, and Android SDK Platform 35 if they are not already installed, let Gradle sync, then run the `app` configuration on an Android 8.0+ emulator or device. No Gradle wrapper is checked in, so use Android Studio's Gradle sync or generate a wrapper after installing Gradle.

## Build an APK on GitHub

1. Push this project to a GitHub repository on the `main` or `master` branch.
2. Open the repository's **Actions** tab and select **Build CamTok Android APK**. The workflow also runs for pushes and pull requests.
3. When the run succeeds, open that workflow run and download the `camtok-debug-apk` artifact.
4. Extract the artifact; it contains `app-debug.apk`. Transfer it to an Android device and install it. Android may ask you to allow installs from the app used to open the APK.

The workflow installs JDK 17, Android SDK 35, Build Tools 35.0.0, and Gradle 8.9, then runs `:app:assembleDebug`. This is a debug APK for testing, not a Play Store release. A release build needs a signing key; keep that key and passwords in GitHub Actions secrets and never commit them to the repository.

The launcher uses adaptive and round icons under `app/src/main/res/mipmap-anydpi-v26`. In-app symbols use the Compose Material icons dependency already declared in `app/build.gradle.kts`.

## Included prototype behavior

- Native feed with vertically browsable clips, category search, For You and Following views, likes, saves, follows, hashtags, and comments.
- Record a video with the Android camera app or import one with the document picker; preview and play it on-device with Media3.
- Explore, Inbox, profile, settings, creator studio, coin wallet, LIVE discovery, and PK match preview screens.
- Demo account, coin, and engagement data is local-only. It is not a real account, recommendation service, broadcast, wallet, or payment.

## Production services still required

Authentication and persistent storage, video upload/transcoding/CDN, personalized ranking, real-time chat and LIVE/PK transport, and moderation must be provided by a server. Digital coin purchases on Android must use Google Play Billing; cash-out requires an audited server and verified payout provider. JazzCash, Easypaisa, PayPal, Wise, and bank transfers are listed as future payout options only. Do not handle payment credentials or trust coin, match, moderation, or anti-fraud decisions in the client.
