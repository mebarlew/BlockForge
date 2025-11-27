package com.blockforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for BlockForge
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class BlockForgeApplication : Application()
