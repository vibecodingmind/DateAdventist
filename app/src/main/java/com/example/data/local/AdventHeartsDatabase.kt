package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserAccountEntity::class,
        ProfileEntity::class,
        LikeEntity::class,
        PassEntity::class,
        MatchEntity::class,
        MessageEntity::class,
        ReportEntity::class,
        BlockEntity::class,
        NotificationEntity::class,
        SystemSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AdventHeartsDatabase : RoomDatabase() {

    abstract fun dao(): AdventHeartsDao

    companion object {
        @Volatile
        private var INSTANCE: AdventHeartsDatabase? = null

        fun getInstance(context: Context): AdventHeartsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AdventHeartsDatabase::class.java,
                    "adventhearts_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
