# BlockForge

Modern Android app for blocking unwanted calls and identifying unknown callers. Free and open-source, no ads.

## Features

### Call Blocking
- **Prefix-based blocking** - block all numbers starting with specific prefixes
  - Country codes (e.g., `+48` blocks all Poland numbers)
  - Area codes (e.g., `555` blocks specific region)
  - Toll-free spam (e.g., `+1-800`)
  - Custom patterns
- Silent rejection of calls from your blocklist
- Blocked call logging with timestamps

### Caller ID
- **Number identification** for incoming calls
- Shows country, carrier, and line type (Mobile/Landline/VoIP)
- **Spam detection** with visual warnings
- Results cached locally for fast lookups
- Overlay appears on incoming calls

### Modern UI
- Material 3 design with custom color scheme
- Dark/Light theme support
- Hero header with blocking statistics
- Tabbed interface (Blocked Prefixes / Call Log)
- Swipe-to-delete on list items
- Bottom sheet for adding prefixes
- Beautiful empty states

## Screenshots

*Coming soon*

## Requirements

- Android 10 (API 29) or higher
- ~10MB storage
- Internet permission (for caller ID lookups)

## Setup

1. Install the app
2. Grant required permissions when prompted
3. Set BlockForge as your default caller ID & spam app in phone settings
4. Add prefixes to block unwanted call ranges

### Caller ID API (Optional)

For full caller ID functionality, get a free API key from [NumVerify](https://numverify.com/) and add it to `CallerIdRepository.kt`:

```kotlin
private const val API_KEY = "your_api_key_here"
```

Free tier includes 100 lookups/month. Without an API key, basic country detection still works.

## How it works

Uses Android's `CallScreeningService` API to:
1. Intercept incoming calls before they ring
2. Check if number matches any blocked prefix
3. Look up caller information (carrier, country, line type)
4. Block matching calls or show caller ID overlay
5. Log all blocked calls for review

The phone may ring briefly (~1 second) before blocking - this is normal API behavior.

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| Min SDK | API 29 (Android 10) |
| Target SDK | API 35 (Android 15) |
| Architecture | MVVM |
| UI | Jetpack Compose + Material 3 |
| Database | Room (SQLite) |
| Networking | Retrofit + OkHttp |
| DI | Hilt |
| Async | Kotlin Coroutines |

## Building

1. Clone the repo
2. Open in Android Studio
3. Sync Gradle
4. Build & run

```bash
./gradlew assembleDebug
```

## Limitations

- Not 100% reliable on all devices (some Samsung/manufacturer-specific issues)
- Only screens non-contact numbers by default
- Brief ring before blocking is expected
- Caller ID accuracy depends on API provider
- Free API tier limited to 100 lookups/month

## Privacy

- All data stored locally on device
- Caller ID lookups sent to NumVerify API (phone numbers only)
- No analytics or tracking
- No ads

## Contributing

Feedback and contributions welcome! Feel free to:
- Open issues for bugs or suggestions
- Submit PRs for improvements
- Help with testing on different devices

## License

MIT

## Status

🚀 **Beta** - Core features working, testing in progress
