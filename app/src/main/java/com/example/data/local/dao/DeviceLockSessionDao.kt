package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DeviceLockSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceLockSessionDao {
    @Query("SELECT * FROM device_lock_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<DeviceLockSessionEntity>>

    @Query("SELECT * FROM device_lock_sessions WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveSessionFlow(): Flow<DeviceLockSessionEntity?>

    @Query("SELECT * FROM device_lock_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): DeviceLockSessionEntity?

    @Query("SELECT * FROM device_lock_sessions WHERE status = 'COMPLETED' ORDER BY endTime DESC")
    fun getCompletedSessions(): Flow<List<DeviceLockSessionEntity>>

    @Query("SELECT * FROM device_lock_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): DeviceLockSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DeviceLockSessionEntity): Long

    @Update
    suspend fun updateSession(session: DeviceLockSessionEntity)

    @Query("UPDATE device_lock_sessions SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateSessionStatus(id: Long, status: String, completedAt: Long?)

    @Query("DELETE FROM device_lock_sessions WHERE status = 'COMPLETED'")
    suspend fun clearCompletedSessions()
}
