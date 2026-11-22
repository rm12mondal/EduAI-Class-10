package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MasterDataEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EduAiDatabase : RoomDatabase() {
    abstract fun masterDataDao(): MasterDataDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: EduAiDatabase? = null

        fun getInstance(context: Context): EduAiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EduAiDatabase::class.java,
                    "eduai_class10_wbbse.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
