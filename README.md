# BlockForge

Modern Android app for blocking unwanted calls and identifying unknown callers. Free and open-source, no ads.

## Features

### Call Blocking
- **Prefix-based blocking** - block all numbers starting with specific prefixes
  - Country codes (e.g., `+48` blocks all Poland numbers)
  - Area codes (e.g., `555` blocks specific region)
  - Toll-free spam (e.g., `+1-800`, `+1-888`, `+1-877`)
  - Custom patterns
- **Block All Mode** - silence all incoming calls with one tap
- **Block Unknown Callers** - only allow calls from your contacts
- **Block International** - block calls from outside your country
- Silent rejection of calls from your blocklist
- Blocked call logging with timestamps and block reason

### Call Log Integration
- **System call log** - view your complete call history in-app
- **Blocked calls tab** - separate view for blocked call history
- Different icons for call types (incoming, outgoing, missed, blocked)
- Tap to call back, long-press for options (SMS, block prefix, copy number)

### Caller ID
- **Number identification** for incoming calls
- Shows country, carrier, and line type (Mobile/Landline/VoIP)
- **Spam detection** with visual warnings
- Results cached locally for fast lookups
- Overlay appears on incoming calls

### Modern UI
- Material 3 design with dynamic theming
- Dark/Light theme support
- **Permission priming** - explains why each permission is needed
- **Protection level indicator** - see your current protection status
- Hero headers with gradient backgrounds
- Tabbed interface (All Calls / Blocked)
- Swipe-to-delete on list items
- Bottom sheet with quick prefix suggestions
- Layered empty states with call-to-action buttons
- **Haptic feedback** throughout the app

## Screenshots

*Coming soon*

## Requirements

- Android 10 (API 29) or higher
- ~10MB storage
- Internet permission (for caller ID lookups)

## Setup

1. Install the app
2. **Permissions screen** guides you through each permission with clear explanations:
   - **Call Screening** (required) - enables call blocking
   - **Phone State** - detect incoming calls
   - **Call Log** - view call history in-app
   - **Contacts** - allow calls from people you know
   - **Make Calls** - call back from the app
   - **Display Over Apps** - show caller ID overlay
3. Progress indicator shows setup completion
4. Add prefixes to block, or use quick suggestions for common spam prefixes

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
- Brief ring before blocking is expected (Android API limitation)
- Caller ID accuracy depends on API provider
- Free API tier limited to 100 lookups/month

## Privacy

- **All data stored locally on device**
- Caller ID lookups sent to NumVerify API (phone numbers only)
- No analytics or tracking
- No ads
- No data collection

## Contributing

Feedback and contributions welcome! Feel free to:
- Open issues for bugs or suggestions
- Submit PRs for improvements
- Help with testing on different devices

## License

MIT

## Status

🚀 **Active Development** - Core features complete, UI polished, testing in progress

### Recent Updates
- Enhanced permissions screen with permission priming
- Improved settings with protection level indicator
- System call log integration with two-tab interface
- Quick prefix suggestions for common spam numbers
- Haptic feedback throughout the app
- Better empty states with call-to-action buttons
