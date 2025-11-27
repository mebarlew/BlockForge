# Android Studio Setup Guide

## Step 1: Open Project in Android Studio

1. Launch **Android Studio** (should be in your Applications folder)
2. On the welcome screen, click **"Open"**
3. Navigate to `/Users/bart/code/BlockForge`
4. Click **"Open"**

## Step 2: First-time Setup (if needed)

If this is your first time using Android Studio:

1. Android Studio will show a wizard for SDK setup
2. Click **"Next"** through the wizard
3. Accept all licenses when prompted
4. Let it download required SDKs (may take 10-30 minutes)

## Step 3: Gradle Sync

After opening BlockForge:

1. Android Studio will automatically start **"Gradle Sync"**
2. You'll see progress at the bottom: "Gradle Build Running..."
3. **First sync takes 5-15 minutes** (downloads dependencies)
4. Wait for it to complete - you'll see "BUILD SUCCESSFUL" or errors

### If Gradle Sync Fails:

1. Click **File → Invalidate Caches → Invalidate and Restart**
2. Wait for Android Studio to restart
3. It will automatically sync again

## Step 4: Project Structure

In the left panel, you should see:

```
BlockForge/
├── app/
│   ├── src/main/java/com/blockforge/
│   │   ├── BlockForgeApplication.kt
│   │   ├── data/
│   │   ├── domain/
│   │   ├── ui/
│   │   └── di/
│   ├── build.gradle.kts
│   └── AndroidManifest.xml
├── build.gradle.kts
└── settings.gradle.kts
```

If you see errors or red squiggly lines, wait for Gradle Sync to finish.

## Step 5: Create Virtual Device (Emulator)

1. Click **Device Manager** icon (phone icon) in top-right toolbar
2. Click **"Create Device"**
3. Select **"Pixel 6"** or any phone
4. Click **"Next"**
5. Download a system image:
   - Select **"Tiramisu (API 33)"** or newer
   - Click **"Download"** next to it
   - Wait for download to complete
6. Click **"Next"** → **"Finish"**

## Step 6: Run the App

1. Make sure virtual device is selected in the toolbar dropdown
2. Click the green **▶ Play** button (or press `Ctrl+R`)
3. **First build takes 3-10 minutes**
4. Emulator will launch (takes 1-2 minutes first time)
5. App should install and open automatically

## Step 7: Enable Call Screening Role

The app needs to be set as a Call Screening app to work:

1. In the emulator, go to **Settings**
2. Search for **"Call Screening"** or **"Phone"**
3. Navigate to **Spam and Call Screen**
4. Select **BlockForge** as the call screening app

## Troubleshooting

### "SDK not found"
- Go to **Android Studio → Preferences → Appearance & Behavior → System Settings → Android SDK**
- Make sure **Android 13.0 (API 33)** and **Android 15.0 (API 35)** are checked
- Click **Apply** and let it download

### "Gradle version mismatch"
- Click **File → Project Structure**
- Check **Gradle Version**: should be 8.7.3 or newer
- Click **OK**

### "KSP version error"
- This usually fixes itself after Gradle sync completes
- If not, click **Build → Clean Project** then **Build → Rebuild Project**

### App crashes on launch
- Check **Logcat** panel (bottom of Android Studio)
- Look for red error messages
- Common issue: Missing permissions - these are declared in AndroidManifest.xml

## File Locations

- **Kotlin code**: `app/src/main/java/com/blockforge/`
- **Database**: `app/src/main/java/com/blockforge/data/database/`
- **UI screens**: `app/src/main/java/com/blockforge/ui/screens/`
- **Call blocking logic**: `app/src/main/java/com/blockforge/domain/CallBlockingService.kt`

## Making Changes

After editing code files in VSCode:

1. **Android Studio auto-detects changes**
2. Click the **Sync** icon (elephant with arrow) if needed
3. Click **▶ Play** to rebuild and run

Changes appear in the emulator immediately after rebuild.

## Testing Call Blocking

1. Open Phone app in emulator
2. Dial a test number (e.g., `+48123456789`)
3. The call should be blocked if `+48` is in your blocklist
4. Check **Logcat** for "Call BLOCKED" or "Call ALLOWED" messages

## Next Steps

- Read `CLAUDE.md` for development guide
- Check `UI_OPTIONS.md` to change UI framework
- Explore the code in `app/src/main/java/com/blockforge/`

## Useful Shortcuts

- **Run app**: `Ctrl+R` (Mac: `Cmd+R`)
- **Build project**: `Ctrl+F9` (Mac: `Cmd+F9`)
- **Find file**: `Ctrl+Shift+N` (Mac: `Cmd+Shift+O`)
- **Search in files**: `Ctrl+Shift+F` (Mac: `Cmd+Shift+F`)

## Resources

- Android Developer Docs: https://developer.android.com/
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Material 3: https://m3.material.io/develop/android/jetpack-compose
- Stack Overflow: https://stackoverflow.com/questions/tagged/android
