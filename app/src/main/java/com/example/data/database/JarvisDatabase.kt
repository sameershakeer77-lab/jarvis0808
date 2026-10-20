package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    TaskEntity::class,
    AuditLogEntity::class,
    CheckpointEntity::class,
    KnowledgeEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao
  abstract fun auditLogDao(): AuditLogDao
  abstract fun checkpointDao(): CheckpointDao
  abstract fun knowledgeDao(): KnowledgeDao

  companion object {
    @Volatile
    private var INSTANCE: JarvisDatabase? = null

    fun getDatabase(context: Context): JarvisDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          JarvisDatabase::class.java,
          "jarvis_core.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
