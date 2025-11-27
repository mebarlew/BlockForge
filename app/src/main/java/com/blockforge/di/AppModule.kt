package com.blockforge.di

import android.content.Context
import androidx.room.Room
import com.blockforge.data.database.AppDatabase
import com.blockforge.data.database.BlockedCallDao
import com.blockforge.data.database.BlockedPrefixDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "blockforge_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBlockedPrefixDao(database: AppDatabase): BlockedPrefixDao {
        return database.blockedPrefixDao()
    }

    @Provides
    fun provideBlockedCallDao(database: AppDatabase): BlockedCallDao {
        return database.blockedCallDao()
    }
}
