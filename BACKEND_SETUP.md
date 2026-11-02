# ELITE MATRIX — Cloud Run, Firebase & FFmpeg Setup Guide

## 1. Architecture Overview
- **Android Client (`app/`)**: Native Kotlin + Jetpack Compose application implementing the Soft Modern Neumorphic UI across all 5 core reference screens, plus Quick Tools (Video Inspector, Preset Manager, Format Converter, Bitrate/Size Calculator), Processing Queue, Projects History, and Settings.
- **Authentication**: Firebase Authentication with Google Sign-In via Android Jetpack `CredentialManager` (`GetSignInWithGoogleOption`), dedicated Email/Password Sign-In, Registration, and Password Reset flows, plus intentional local-only Guest Mode.
- **Persistence**:
  - **Local**: Room Database (`EliteMatrixDatabase`) for offline-first presets, jobs, and export records + DataStore Preferences for theme palette, dark/light mode, and routing settings.
  - **Cloud**: Cloud Firestore (`ai-studio-android-elitemat-0bba95f5-5762-42d5-a783-b114841e7623`) with zero-trust ownership rules (`firestore.rules`) across:
    - `users/{uid}`
    - `users/{uid}/presets/{presetId}`
    - `users/{uid}/jobs/{jobId}`
    - `users/{uid}/exports/{exportId}`
    - `users/{uid}/settings/preferences`
- **Media Processing Engine**:
  - **On-Device**: Hardware-accelerated AndroidX Media3 `Transformer` + OpenGL Shader Effects (`RgbAdjustment`, `Contrast`, `HslAdjustment`, `ScaleAndRotateTransformation`, `Presentation`) + `MediaMetadataRetriever` / `MediaExtractor` inspector + FFmpeg CLI command builder & binary executor.
  - **Cloud Run Worker (`backend/`)**: Node.js + maintained FFmpeg/FFprobe container verifying Firebase ID tokens, validating inputs with `ffprobe`, executing `ffmpeg` filter graphs (`eq`, `colorbalance`, `unsharp`, `hqdn3d`, `scale`, `libx264`/`libx265`/`libsvtav1`), and updating job progress in Firestore.

## 2. Deploying the Cloud Run FFmpeg Worker
```bash
gcloud builds submit ./backend --tag gcr.io/gen-lang-client-0371773990/elite-matrix-worker
gcloud run deploy elite-matrix-worker \
  --image gcr.io/gen-lang-client-0371773990/elite-matrix-worker \
  --region asia-southeast1 \
  --platform managed \
  --no-allow-unauthenticated \
  --memory 4Gi \
  --cpu 2 \
  --timeout 900s \
  --set-env-vars="FIRESTORE_DATABASE_ID=ai-studio-android-elitemat-0bba95f5-5762-42d5-a783-b114841e7623,STORAGE_BUCKET=gen-lang-client-0371773990.firebasestorage.app"
```
