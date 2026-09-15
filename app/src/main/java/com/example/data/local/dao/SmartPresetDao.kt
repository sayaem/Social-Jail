package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SmartPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartPresetDao {
    @Query("SELECT * FROM smart_presets ORDER BY id ASC")
    fun getAllPresets(): Flow<List<SmartPresetEntity>>

    @Query("SELECT * FROM smart_presets")
    suspend fun getAllPresetsList(): List<SmartPresetEntity>

    @Query("SELECT * FROM smart_presets WHERE id = :id")
    suspend fun getPresetById(id: Long): SmartPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: SmartPresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(presets: List<SmartPresetEntity>)

    @Update
    suspend fun updatePreset(preset: SmartPresetEntity)

    @Query("DELETE FROM smart_presets WHERE id = :id")
    suspend fun deletePreset(id: Long)
}
