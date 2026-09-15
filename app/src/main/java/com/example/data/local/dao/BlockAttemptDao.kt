package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.local.entity.BlockAttemptEntity
import kotlinx.coroutines.flow.Flow

data class AppAttemptCount(
    val packageName: String,
    val appName: String,
    val count: Int
)

@Dao
interface BlockAttemptDao {
    @Insert
    suspend fun recordAttempt(attempt: BlockAttemptEntity): Long

    @Query("SELECT COUNT(*) FROM block_attempts WHERE sessionId = :sessionId")
    fun getSessionAttemptCount(sessionId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM block_attempts WHERE sessionId = :sessionId")
    suspend fun getSessionAttemptCountNow(sessionId: Long): Int

    @Query("SELECT packageName, appName, COUNT(*) as count FROM block_attempts WHERE sessionId = :sessionId GROUP BY packageName ORDER BY count DESC")
    fun getSessionAttemptsByApp(sessionId: Long): Flow<List<AppAttemptCount>>

    @Query("SELECT COUNT(*) FROM block_attempts WHERE timestamp >= :sinceTimestamp")
    fun getAttemptsSince(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT packageName, appName, COUNT(*) as count FROM block_attempts WHERE timestamp >= :sinceTimestamp GROUP BY packageName ORDER BY count DESC")
    fun getAttemptsByAppSince(sinceTimestamp: Long): Flow<List<AppAttemptCount>>

    @Query("SELECT * FROM block_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<BlockAttemptEntity>>

    @Query("SELECT * FROM block_attempts ORDER BY timestamp DESC")
    suspend fun getAllAttemptsList(): List<BlockAttemptEntity>

    @Query("SELECT COUNT(*) FROM block_attempts")
    fun getTotalAttemptCount(): Flow<Int>

    @Query("SELECT packageName, appName, COUNT(*) as count FROM block_attempts GROUP BY packageName ORDER BY count DESC LIMIT 1")
    fun getMostAttemptedApp(): Flow<AppAttemptCount?>

    @Query("SELECT packageName, appName, COUNT(*) as count FROM block_attempts GROUP BY packageName ORDER BY count DESC")
    fun getAllAttemptsByApp(): Flow<List<AppAttemptCount>>

    @Query("DELETE FROM block_attempts")
    suspend fun clearAllAttempts()
}
