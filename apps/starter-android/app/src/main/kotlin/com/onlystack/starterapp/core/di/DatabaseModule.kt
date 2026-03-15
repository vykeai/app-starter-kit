package com.onlystack.starterapp.core.di

import android.content.Context
import androidx.room.Room
import com.onlystack.starterapp.core.db.AppDatabase
import com.onlystack.starterapp.core.db.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
            .addMigrations(MIGRATION_1_2)
            .build()
}
