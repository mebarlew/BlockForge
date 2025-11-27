# BlockForge

Simple Android app for blocking unwanted calls. Free and open-source, no ads.

## What it does

- Blocks spam calls using Android's CallScreeningService
- **Prefix-based blocking** - block all numbers starting with specific prefixes (e.g., `+48`, `+1-800`, `555`)
- Silent rejection of calls from your blocklist
- Local storage - no data sent anywhere
- Works alongside your existing phone app

## Features

### Prefix Blocking
Block entire ranges of numbers by prefix:
- Country codes (e.g., `+48` blocks all Poland numbers)
- Area codes (e.g., `555` blocks specific region)
- Toll-free spam (e.g., `+1-800`)
- Custom patterns

Example: Add `+48` to blocklist → all calls starting with `+48` are rejected

## Requirements

- Android 10 (API 29) or higher
- ~5MB storage

## How it works

Uses Android's `CallScreeningService` API to intercept and reject unwanted calls. The phone may ring briefly (~1 second) before blocking - this is normal API behavior.

## Limitations

- Not 100% reliable on all devices (some Samsung/manufacturer-specific issues reported)
- Only screens non-contact numbers by default
- Brief ring before blocking is expected
- Works on ~95% of Android 10+ devices

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: API 29 (Android 10)
- **Target SDK**: API 35 (Android 15)
- **Architecture**: MVVM
- **Database**: Room (SQLite)
- **UI**: Jetpack Compose
- **DI**: Hilt

## Building

1. Clone the repo
2. Open in Android Studio
3. Sync Gradle
4. Build & run

## About

Built by a JavaScript developer learning Android development with significant help from Claude AI. This is my first Android project, so feedback and contributions are highly appreciated!

Feel free to open issues or submit PRs if you find bugs or have suggestions.

## License

MIT

## Status

🚧 **In Development** - Not yet functional
