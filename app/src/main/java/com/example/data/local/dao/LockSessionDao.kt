package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LockSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockSessionDao {
    @Query("SELECT * FROM lock_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<LockSessionEntity>>

    @Query("SELECT * FROM lock_sessions WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveSessionFlow(): Flow<LockSessionEntity?>

    @Query("SELECT * FROM lock_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): LockSessionEntity?

    @Query("SELECT * FROM lock_sessions WHERE status = 'SCHEDULED' ORDER BY startTime ASC")
    fun getScheduledSessions(): Flow<List<LockSessionEntity>>

    @Query("SELECT * FROM lock_sessions WHERE status = 'SCHEDULED' ORDER BY startTime ASC")
    suspend fun getScheduledSessionsList(): List<LockSessionEntity>

    @Query("SELECT * FROM lock_sessions WHERE status = 'COMPLETED' ORDER BY endTime DESC")
    fun getCompletedSessions(): Flow<List<LockSessionEntity>>

    @Query("SELECT * FROM lock_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): LockSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LockSessionEntity): Long

    @Update
    suspend fun updateSession(session: LockSessionEntity)

    @Query("UPDATE lock_sessions SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateSessionStatus(id: Long, status: String, completedAt: Long?)

    @Query("UPDATE lock_sessions SET goalStatus = :goalStatus, reviewNote = :reviewNote WHERE id = :id")
    suspend fun updateSessionReview(id: Long, goalStatus: String?, reviewNote: String?)

    @Query("UPDATE lock_sessions SET escalationTriggered = 1 WHERE id = :id")
    suspend fun markEscalationTriggered(id: Long)

    @Query("DELETE FROM lock_sessions WHERE id = :id AND status = 'SCHEDULED'")
    suspend fun deleteScheduledSession(id: Long)

    @Query("DELETE FROM lock_sessions WHERE status = 'COMPLETED'")
    suspend fun clearCompletedSessions()
}
