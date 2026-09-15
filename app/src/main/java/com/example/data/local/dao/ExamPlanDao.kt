package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ExamPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamPlanDao {
    @Query("SELECT * FROM exam_plans ORDER BY examDateMillis ASC")
    fun getAllExamPlans(): Flow<List<ExamPlanEntity>>

    @Query("SELECT * FROM exam_plans WHERE isActive = 1 ORDER BY examDateMillis ASC LIMIT 1")
    fun getActiveExamPlan(): Flow<ExamPlanEntity?>

    @Query("SELECT * FROM exam_plans WHERE id = :id")
    suspend fun getExamPlanById(id: Long): ExamPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamPlan(plan: ExamPlanEntity): Long

    @Update
    suspend fun updateExamPlan(plan: ExamPlanEntity)

    @Query("DELETE FROM exam_plans WHERE id = :id")
    suspend fun deleteExamPlan(id: Long)
}
