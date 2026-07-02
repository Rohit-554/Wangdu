# Wangdu 

A real time collaborative whiteboard built with Kotlin Multiplatform and Compose Multiplatform. Multiple clients draw on a shared canvas, see each other's live cursors, and stay in sync over WebSockets with server side persistence.

# Special Mentions 
- Template Generated via Catylst KMP Starter (Android Studio Plugin)

## Badges

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.0--rc01-4285F4?logo=jetpackcompose&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-3.4.3-087CFA?logo=ktor&logoColor=white)
![Koin](https://img.shields.io/badge/Koin-4.1.1-F9A825)
![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Desktop-2C3E50)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## Demo

<img width="1821" height="991" alt="Screenshot 2026-07-02 at 1 46 01 AM" src="https://github.com/user-attachments/assets/c2b99bc2-5a17-40a6-8b45-b226b6a9b661" />

## Features

- Real time collaborative drawing synced across all connected clients
- Live cursor presence so you can see where others are pointing
- Broadcasting of stroke events to every client over WebSockets
- Server side persistence so boards survive restarts and late joiners
- Configurable display name on join
- Shared toolbar for drawing controls

## Tech Stack

- **Language:** Kotlin 2.3.21
- **UI:** Compose Multiplatform 1.11.0-rc01, Material3 1.11.0-alpha06
- **Networking:** Ktor 3.4.3 (client and server, WebSockets)
- **Dependency Injection:** Koin 4.1.1
- **Navigation:** Navigation3 1.1.1
- **Serialization:** kotlinx.serialization 1.9.0
- **Persistence:** Exposed 0.56.0 with SQLite JDBC 3.47.1.0
- **Concurrency:** kotlinx.coroutines 1.10.2
- **Build:** Gradle with AGP 9.1.1, KSP 2.3.7

## Platforms

- Android
- iOS
- Desktop (JVM)

## Modules

- `composeApp` — shared Compose Multiplatform UI and client logic
- `androidApp` — Android application entry point
- `server` — Ktor WebSocket server with persistence
- `shared` — models and code shared between client and server

## Getting Started

### Prerequisites

- JDK 17 or newer
- Android Studio or IntelliJ IDEA (latest stable)
- Xcode (only required for the iOS target)

### Setup

1. Clone the repository
2. Open the project in Android Studio or IntelliJ IDEA
3. Sync Gradle

### Run the server

```bash
./gradlew :server:run
```

### Run the Android app

```bash
./gradlew :androidApp:assembleDebug
```

### Run the Desktop app

```bash
./gradlew :composeApp:run
```

### Run the iOS app

Open the iOS project in Xcode and run it on a simulator or device.

## Before Going to Production

This project is built for learning. The choices below keep it simple to run on a single machine, but they are not safe or scalable for real users. Treat this as a map of what to change before shipping.

1. **Replace SQLite with Postgres.** SQLite is a single file on disk. It locks on concurrent writes, so many users drawing at once contend for the same file. Postgres handles concurrent writes safely and provides proper backups and replication.

2. **Switch from ws:// to wss://.** Plain WebSocket traffic is unencrypted, so anyone on the network path can read or tamper with it. Production must use wss:// (WebSocket Secure), which needs an SSL certificate on the server, usually terminated by a reverse proxy such as Nginx or Caddy.

3. **Add authentication.** Any client can currently connect and claim any display name. Users should sign in and the server should verify their identity before accepting events, so names and actions can be trusted.

4. **Add room support.** Every connected user shares one global board today. A real app gives each board its own room with a separate WebSocket route, session registry, and database partition, so boards stay isolated.

5. **Add rate limiting.** A malicious or buggy client could send thousands of stroke events per second and overload the server. Limiting the event rate per session protects the server and keeps one client from degrading the experience for everyone.

6. **Handle server restarts gracefully.** If the server restarts, clients currently lose their connection with no explanation. Clients should detect the disconnect and automatically reconnect with exponential backoff so a restart is a brief blip rather than a dead app.

7. **Add stroke count limits.** The database currently stores strokes forever, so a board grows without bound. Production boards should cap the number of strokes or expire old ones by age to keep storage and load times under control.

## Future Improvements

Beyond production hardening, here are ideas to build on top of this project:

1. **Undo and redo.** Track a per user history of stroke operations so a client can revert its own actions without affecting others.

2. **Multiple boards and a lobby.** Add a home screen listing available boards, creating new ones, and joining by code, backed by the room support described above.

3. **Richer drawing tools.** Add shapes, text, arrows, an eraser, fill, and adjustable stroke width and opacity beyond freehand drawing.

4. **Export and share.** Let users export a board as an image or vector file, or share a read only snapshot link.

5. **Infinite and zoomable canvas.** Support panning and zooming on a large virtual canvas with viewport based rendering for performance.

6. **Presence and chat.** Show an avatar list of who is online and add a lightweight text chat alongside the board.

7. **Conflict free sync with CRDTs.** Move from broadcast based sync to a CRDT model so offline edits merge cleanly when clients reconnect.

8. **Media on the canvas.** Allow dropping images or sticky notes onto the board, uploaded to object storage and referenced by URL.

9. **Web target.** Extend the Compose Multiplatform app to run in the browser via Kotlin/Wasm so the whiteboard works without an install.

10. **Playback and versioning.** Persist an event log so a board can be replayed over time or restored to an earlier state.

## Contributing

Contributions are welcome. Please read the [Contributing Guidelines](CONTRIBUTING.md) to get started, and note that this project follows a [Code of Conduct](CODE_OF_CONDUCT.md).

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
