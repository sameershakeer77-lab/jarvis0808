package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
  @PrimaryKey val taskId: String,
  val userPrompt: String,
  val classification: String,
  val summary: String,
  val status: String,
  val stepsCompleted: Int,
  val totalSteps: Int,
  val createdAt: Long,
  val completedAt: Long?,
  val modelUsed: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val taskId: String,
  val toolName: String,
  val permissionLevel: Int,
  val inputPayload: String,
  val outputPayload: String,
  val success: Boolean,
  val errorMessage: String?,
  val timestamp: Long
)

@Entity(tableName = "checkpoints")
data class CheckpointEntity(
  @PrimaryKey val checkpointId: String,
  val taskId: String,
  val description: String,
  val snapshotSummary: String,
  val timestamp: Long
)

@Entity(tableName = "project_knowledge")
data class KnowledgeEntity(
  @PrimaryKey val key: String,
  val category: String,
  val title: String,
  val content: String,
  val updatedTimestamp: Long
)
