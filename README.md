# PixelVerse (InstaClone)

PixelVerse is a modern, feature-rich social media application inspired by Instagram, built completely natively with Kotlin and Jetpack Compose. 

## Key Features

* **Home Feed (Mixed Media)**: A highly interactive feed supporting both high-quality photos and streaming videos, automatically playing as you scroll.
* **Snaplies (Stories)**: Share fleeting moments with Snaplies! A dedicated horizontal feed at the top of the app allowing users to upload and view full-screen vertical ephemeral content.
* **CameraX Integration**: Take photos directly from the app without leaving the experience using a custom, high-performance CameraX interface.
* **Firebase Backend**: Real-time synchronization of posts, media uploads to Firebase Storage, and secure Firestore database integration for a true SaaS-ready experience.
* **Jetpack Compose UI**: Built with a 100% declarative UI leveraging the full power of Material Design 3 and seamless animations (like the iconic double-tap-to-heart!).
* **Local Persistence**: Powered by Room Database for fast, offline-first caching of posts and interactions.

## Architecture & Tech Stack

- **UI Framework**: Jetpack Compose (Material 3)
- **Local Database**: Room
- **Backend/Storage**: Firebase Firestore, Firebase Storage
- **Media**: ExoPlayer (androidx.media3) for seamless video playback, Coil for image loading
- **Camera**: AndroidX CameraX
- **Concurrency**: Kotlin Coroutines & Flows
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture

Enjoy exploring PixelVerse, the next evolution of visual sharing!
