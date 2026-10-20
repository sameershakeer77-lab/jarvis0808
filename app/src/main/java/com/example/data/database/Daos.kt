package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
  fun getAllTasks(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
  suspend fun getTaskById(taskId: String): TaskEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTask(task: TaskEntity)

  @Update
  suspend fun updateTask(task: TaskEntity)

  @Query("DELETE FROM tasks")
  suspend fun clearTasks()
}

@Dao
interface AuditLogDao {
  @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
  fun getAllLogs(): Flow<List<AuditLogEntity>>

  @Query("SELECT * FROM audit_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
  fun getLogsForTask(taskId: String): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: AuditLogEntity)

  @Query("DELETE FROM audit_logs")
  suspend fun clearLogs()
}

@Dao
interface CheckpointDao {
  @Query("SELECT * FROM checkpoints ORDER BY timestamp DESC")
  fun getAllCheckpoints(): Flow<List<CheckpointEntity>>

  @Query("SELECT * FROM checkpoints WHERE checkpointId = :checkpointId LIMIT 1")
  suspend fun getCheckpoint(checkpointId: String): CheckpointEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCheckpoint(checkpoint: CheckpointEntity)

  @Query("DELETE FROM checkpoints WHERE checkpointId = :checkpointId")
  suspend fun deleteCheckpoint(checkpointId: String)
}

@Dao
interface KnowledgeDao {
  @Query("SELECT * FROM project_knowledge ORDER BY category, title")
  fun getAllKnowledge(): Flow<List<KnowledgeEntity>>

  @Query("SELECT * FROM project_knowledge WHERE `key` = :key LIMIT 1")
  suspend fun getKnowledge(key: String): KnowledgeEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertKnowledge(knowledge: KnowledgeEntity)

  @Query("DELETE FROM project_knowledge WHERE `key` = :key")
  suspend fun deleteKnowledge(key: String)

  @Query("DELETE FROM project_knowledge")
  suspend fun clearKnowledge()
}
