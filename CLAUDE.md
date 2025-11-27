# Claude Development Guide for BlockForge

## Project Overview
BlockForge is an Android spam call blocker app using CallScreeningService API.

## Architecture

### Tech Stack
- **Language**: Kotlin
- **Min SDK**: API 29 (Android 10)
- **Target SDK**: API 35 (Android 15)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **UI**: Jetpack Compose (or alternative - TBD)
- **DI**: Hilt
- **Build**: Gradle with Kotlin DSL

### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/blockforge/
│   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   │   ├── BlockedPrefix.kt (Entity)
│   │   │   │   │   ├── BlockedPrefixDao.kt
│   │   │   │   │   └── AppDatabase.kt
│   │   │   │   └── repository/
│   │   │   │       └── BlocklistRepository.kt
│   │   │   ├── domain/
│   │   │   │   └── CallBlockingService.kt (CallScreeningService)
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── BlocklistScreen.kt
│   │   │   │   │   └── AddPrefixScreen.kt
│   │   │   │   └── viewmodel/
│   │   │   │       └── BlocklistViewModel.kt
│   │   │   └── di/
│   │   │       └── AppModule.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── test/
├── build.gradle.kts
└── proguard-rules.pro
```

## Core Features

### 1. Prefix-based Call Blocking
- Block calls by prefix (e.g., `+48`, `+1-800`, `555`)
- Store prefixes in Room database
- Check incoming calls against prefix list

### 2. CallScreeningService Implementation
```kotlin
class CallBlockingService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle.schemeSpecificPart
        val shouldBlock = checkIfBlocked(number)

        val response = CallResponse.Builder()
            .setDisallowCall(shouldBlock)
            .setRejectCall(shouldBlock)
            .build()

        respondToCall(callDetails, response)
    }
}
```

### 3. Database Schema
```kotlin
@Entity(tableName = "blocked_prefixes")
data class BlockedPrefix(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "prefix") val prefix: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
```

## Implementation Guidelines

### Must-haves
- CallScreeningService responds within 5 seconds
- Efficient database queries (indexed prefix column)
- Permission handling (CALL_SCREENING role)
- Works on Android 10+

### Nice-to-haves
- Import/export blocklist
- Statistics (blocked calls count)
- Notification on blocked call
- Whitelist for specific numbers

## Permissions Required
```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
```

## Play Store Requirements
- Target API 35 (Android 15) by Aug 2025
- Privacy policy (no data collection)
- AAB format for release
- Comply with CallScreeningService policies

## Development Notes
- Developer is JavaScript background, new to Android/Kotlin
- Keep code simple and well-commented
- Follow Kotlin conventions and best practices
- Test on Android 10+ devices/emulators

## Testing Strategy
- Unit tests for prefix matching logic
- Integration tests for Room database
- Manual testing with real calls on device
- Test edge cases (international numbers, special chars)

## Next Steps
1. Set up basic Android project structure
2. Implement Room database with BlockedPrefix entity
3. Create CallScreeningService
4. Build simple UI for managing prefixes
5. Test on real device
6. Prepare for Play Store submission
