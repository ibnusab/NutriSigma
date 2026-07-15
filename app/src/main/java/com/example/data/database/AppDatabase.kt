package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ArticleDao
import com.example.data.dao.HistoryDao
import com.example.data.dao.ReminderDao
import com.example.data.dao.UserDao
import com.example.data.model.ArticleEntity
import com.example.data.model.HistoryEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        HistoryEntity::class,
        ReminderEntity::class,
        ArticleEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun historyDao(): HistoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutri_sigma_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
