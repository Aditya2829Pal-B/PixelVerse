# PixelVerse (InstaClone)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.1.1-green.svg)](https://developer.android.com/studio/releases/gradle-plugin)
[![Compose BOM](https://img.shields.io/badge/Compose_BOM-2024.09.00-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Compile SDK](https://img.shields.io/badge/compileSdk-36-orange.svg)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/minSdk-24-lightgrey.svg)](https://developer.android.com)

PixelVerse is a modern, feature-rich social media application inspired by Instagram, built completely natively with Kotlin and Jetpack Compose.

---

## Key Features

* **Home Feed (Mixed Media)**: A highly interactive feed supporting both high-quality photos and streaming videos, automatically playing as you scroll.
* **Snaplies (Stories)**: Share fleeting moments with Snaplies! A dedicated horizontal feed at the top of the app allowing users to upload and view full-screen vertical ephemeral content.
* **CameraX Integration**: Capture photos directly from the app without leaving the experience using a custom, high-performance CameraX interface.
* **Firebase Backend**: Real-time synchronization of posts, media uploads to Firebase Storage, and secure Firestore database integration for a true SaaS-ready experience.
* **Jetpack Compose UI**: Built with a 100% declarative UI leveraging Material Design 3 and smooth animations (such as the iconic double-tap-to-heart!).
* **Local Persistence**: Powered by Room Database for fast, offline-first caching of posts and interactions.

---

## Architecture & Tech Stack

- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Concurrency**: Kotlin Coroutines & Asynchronous Flows
- **Local Database**: AndroidX Room (v2.7.0)
- **Backend / Storage**: Firebase Firestore, Firebase Storage, Firebase Auth
- **Media & Camera**: ExoPlayer (`androidx.media3`), AndroidX CameraX (Camera2 / Lifecycle / View)
- **Image Loading**: Coil Compose (v2.7.0)
- **Testing & Verification**: JUnit 4, AndroidX Test, Robolectric, Roborazzi

---

## Project Structure

```text
PixelVerse/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/       # Core business logic, UI screens, ViewModels, Repository
│   │   │   ├── res/                    # Drawables, layouts, values, and themes
│   │   │   └── AndroidManifest.xml     # App permissions and activity registration
│   │   ├── test/                       # Unit tests (Robolectric & JUnit)
│   │   └── androidTest/                # Instrumented UI and Compose tests
│   ├── build.gradle.kts                # Application Gradle module configuration
│   └── proguard-rules.pro              # Proguard obfuscation rules
├── gradle/
│   └── libs.versions.toml              # Centralized version catalog
├── AGENTS.md                           # Agent rules & naming conventions (Snaply, Auth)
├── .env.example                        # Environment variable template for secrets
└── build.gradle.kts                    # Root build configuration
```

---

## Prerequisites & Environment Setup

Before building PixelVerse locally, ensure your environment meets the following specifications:

| Requirement | Recommended Version |
|---|---|
| **Android Studio** | Ladybug Feature Drop (2024.2+) or newer |
| **JDK** | Java Development Kit (JDK) 17 or 21 (compatible with Java 11 bytecode target) |
| **Android SDK** | Compile SDK `36`, Target SDK `36`, Min SDK `24` |
| **Build Tools** | Android SDK Build-Tools `36.0.0` or compatible |
| **Gradle** | Configured via included Gradle Wrapper (`gradlew`) |

---

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/Aditya2829Pal-B/PixelVerse.git
cd PixelVerse
```

### 2. Configure Secrets & Environment Variables
Copy the sample `.env.example` file to create your local `.env` configuration (which is gitignored):
```bash
cp .env.example .env
```
Add your optional API keys (such as `GEMINI_API_KEY`) inside `.env`:
```env
GEMINI_API_KEY=your_actual_gemini_api_key_here
```

### 3. Firebase Configuration
PixelVerse uses Firebase for Firestore database and storage sync:
1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
2. Register an Android application with Package Name: `com.aistudio.instaclone.abcxyz`.
3. Download the generated `google-services.json` file.
4. Place `google-services.json` in the `app/` directory (`PixelVerse/app/google-services.json`).
> ⚠️ **Note:** `google-services.json` contains project credentials and must **never** be committed to public version control. It is ignored in `.gitignore`.

---

## Building & Running the App

### Via Android Studio
1. Open Android Studio and select **Open**, then choose the `PixelVerse` directory.
2. Wait for Gradle Sync to complete.
3. Select an Android Emulator running API 24 or higher (API 34+ recommended) or connect a physical Android device with USB Debugging enabled.
4. Click the **Run** button (`Shift + F10`) or choose **Run 'app'**.

### Via Command Line (Gradle)

- **Build Debug APK:**
  ```bash
  ./gradlew assembleDebug
  # On Windows:
  gradlew.bat assembleDebug
  ```

- **Install on Connected Device / Emulator:**
  ```bash
  ./gradlew installDebug
  # On Windows:
  gradlew.bat installDebug
  ```

---

## Running Tests

- **Run Unit Tests (Local JVM):**
  ```bash
  ./gradlew testDebugUnitTest
  # On Windows:
  gradlew.bat testDebugUnitTest
  ```

- **Run Android Instrumented Tests (Requires Emulator / Device):**
  ```bash
  ./gradlew connectedDebugAndroidTest
  # On Windows:
  gradlew.bat connectedDebugAndroidTest
  ```

- **Run Screenshot / Snapshot Tests (Roborazzi):**
  ```bash
  ./gradlew verifyRoborazziDebug
  # On Windows:
  gradlew.bat verifyRoborazziDebug
  ```

---

## Known Limitations & Mock Backends
- **Sample Feed Data**: By default, feed images and exploratory media leverage mock URLs (such as Picsum / placeholder endpoints) when no live Firestore collection exists.
- **Story Terminology**: Per `AGENTS.md`, all ephemeral stories are formally referred to and implemented as **"Snaply" / "Snaplies"**.

---

## Contribution Workflow

We welcome community contributions! Please adhere to the following workflow:

1. **Fork** the repository and clone your fork locally.
2. Create a new topic branch for your change:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```
3. Make your changes according to the architectural patterns (MVVM, Clean Architecture, Jetpack Compose best practices).
4. Run tests and verify that the app compiles cleanly:
   ```bash
   ./gradlew testDebugUnitTest
   ```
5. Commit your changes with clear, descriptive commit messages.
6. Push to your fork and open a **Pull Request** against `main` explaining the problem solved, implementation details, and screenshots if UI changes were made.
