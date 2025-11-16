# TestMap - Android Maps Application

A simple Android application with Google Maps displaying Krakow city map.

## Quick Setup

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/TestMap.git
2. Setup API Key
Create local.properties file in project root and add:

Windows:

properties
sdk.dir=C\:\\Users\\USERNAME\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY="YOUR_GOOGLE_MAPS_API_KEY"

3. Get Google Maps API Key
Go to Google Cloud Console

Create API key with restrictions:

APIs: Maps SDK for Android

Package name: your app package name

SHA-1: from ./gradlew signingReport

4. Run Application
Open project in Android Studio

File → Sync Project with Gradle Files

Run on device/emulator
