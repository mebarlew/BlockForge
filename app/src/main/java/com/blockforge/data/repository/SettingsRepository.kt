package com.blockforge.data.repository

import com.blockforge.data.database.BlockingSettings
import com.blockforge.data.database.BlockingSettingsDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing blocking settings
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: BlockingSettingsDao
) {

    fun getSettings(): Flow<BlockingSettings?> = settingsDao.getSettings()

    suspend fun getSettingsOnce(): BlockingSettings {
        return settingsDao.getSettingsOnce() ?: BlockingSettings()
    }

    suspend fun toggleBlockUnknown(enabled: Boolean) {
        settingsDao.toggleBlockUnknown(enabled)
    }

    suspend fun toggleBlockAll(enabled: Boolean) {
        settingsDao.toggleBlockAll(enabled)
    }

    suspend fun toggleBlockInternational(enabled: Boolean) {
        settingsDao.toggleBlockInternational(enabled)
    }

    suspend fun updateUserCountryCode(countryCode: String) {
        val current = getSettingsOnce()
        settingsDao.updateSettings(current.copy(userCountryCode = countryCode))
    }
}
