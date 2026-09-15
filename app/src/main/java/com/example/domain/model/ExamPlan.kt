package com.example.domain.model

import com.example.data.local.entity.ExamPlanEntity
import java.util.concurrent.TimeUnit

data class ExamPlan(
    val id: Long = 0,
    val title: String,
    val examDateMillis: Long,
    val targetDailyHours: Int = 6,
    val profileName: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun daysRemaining(now: Long = System.currentTimeMillis()): Long {
        val diff = examDateMillis - now
        return if (diff <= 0) 0L else TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun toEntity(): ExamPlanEntity {
        return ExamPlanEntity(
            id = id,
            title = title,
            examDateMillis = examDateMillis,
            targetDailyHours = targetDailyHours,
            profileName = profileName,
            notes = notes,
            isActive = isActive,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromEntity(entity: ExamPlanEntity): ExamPlan {
            return ExamPlan(
                id = entity.id,
                title = entity.title,
                examDateMillis = entity.examDateMillis,
                targetDailyHours = entity.targetDailyHours,
                profileName = entity.profileName,
                notes = entity.notes,
                isActive = entity.isActive,
                createdAt = entity.createdAt
            )
        }
    }
}
