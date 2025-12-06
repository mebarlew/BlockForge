package com.blockforge.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for blocking settings
 */
@Dao
interface BlockingSettingsDao {

    @Query("SELECT * FROM blocking_settings WHERE id = 1")
    fun getSettings(): Flow<BlockingSettings?>

    @Query("SELECT * FROM blocking_settings WHERE id = 1")
    suspend fun getSettingsOnce(): BlockingSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: BlockingSettings)

    @Transaction
    suspend fun toggleBlockUnknown(enabled: Boolean) {
        val current = getSettingsOnce() ?: BlockingSettings()
        updateSettings(current.copy(blockUnknown = enabled))
    }

    @Transaction
    suspend fun toggleBlockAll(enabled: Boolean) {
        val current = getSettingsOnce() ?: BlockingSettings()
        updateSettings(current.copy(blockAll = enabled))
    }

    @Transaction
    suspend fun toggleBlockInternational(enabled: Boolean) {
        val current = getSettingsOnce() ?: BlockingSettings()
        updateSettings(current.copy(blockInternational = enabled))
    }
}
