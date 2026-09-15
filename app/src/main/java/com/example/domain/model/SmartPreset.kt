package com.example.domain.model

import com.example.data.local.entity.SmartPresetEntity

data class SmartPreset(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val description: String,
    val durationMinutes: Int,
    val commitmentLevel: CommitmentLevel = CommitmentLevel.HARDCORE,
    val defaultGoal: String? = null,
    val escalationEnabled: Boolean = false,
    val escalationAttemptTrigger: Int = 3,
    val blockedPackageNames: List<String> = emptyList(),
    val profileName: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): SmartPresetEntity {
        return SmartPresetEntity(
            id = id,
            name = name,
            emoji = emoji,
            description = description,
            durationMinutes = durationMinutes,
            commitmentLevel = commitmentLevel.name,
            defaultGoal = defaultGoal,
            escalationEnabled = escalationEnabled,
            escalationAttemptTrigger = escalationAttemptTrigger,
            blockedPackageNames = blockedPackageNames.joinToString(","),
            profileName = profileName,
            isDefault = isDefault,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromEntity(entity: SmartPresetEntity): SmartPreset {
            val pkgs = if (entity.blockedPackageNames.isBlank()) emptyList() else entity.blockedPackageNames.split(",")
            val level = try {
                CommitmentLevel.valueOf(entity.commitmentLevel)
            } catch (e: Exception) {
                CommitmentLevel.HARDCORE
            }
            return SmartPreset(
                id = entity.id,
                name = entity.name,
                emoji = entity.emoji,
                description = entity.description,
                durationMinutes = entity.durationMinutes,
                commitmentLevel = level,
                defaultGoal = entity.defaultGoal,
                escalationEnabled = entity.escalationEnabled,
                escalationAttemptTrigger = entity.escalationAttemptTrigger,
                blockedPackageNames = pkgs,
                profileName = entity.profileName,
                isDefault = entity.isDefault,
                createdAt = entity.createdAt
            )
        }

        fun getDefaultPresets(): List<SmartPreset> {
            return listOf(
                SmartPreset(
                    id = 1,
                    name = "Study",
                    emoji = "📚",
                    description = "Focused textbook, lecture & assignment study",
                    durationMinutes = 120,
                    commitmentLevel = CommitmentLevel.HARDCORE,
                    defaultGoal = "Study Session",
                    escalationEnabled = true,
                    escalationAttemptTrigger = 3,
                    profileName = "Study",
                    isDefault = true
                ),
                SmartPreset(
                    id = 2,
                    name = "Deep Work",
                    emoji = "💻",
                    description = "High-output programming and analytical work",
                    durationMinutes = 90,
                    commitmentLevel = CommitmentLevel.HARDCORE,
                    defaultGoal = "Deep Work Sprint",
                    escalationEnabled = true,
                    escalationAttemptTrigger = 3,
                    profileName = "Productivity",
                    isDefault = true
                ),
                SmartPreset(
                    id = 3,
                    name = "Exam",
                    emoji = "🧠",
                    description = "High stakes test prep & MCQ solve sprints",
                    durationMinutes = 180,
                    commitmentLevel = CommitmentLevel.HARDCORE,
                    defaultGoal = "Exam Preparation",
                    escalationEnabled = true,
                    escalationAttemptTrigger = 2,
                    profileName = "Study",
                    isDefault = true
                ),
                SmartPreset(
                    id = 4,
                    name = "Sleep",
                    emoji = "😴",
                    description = "Night lock: 11:30 PM to 6:30 AM restorative rest",
                    durationMinutes = 420,
                    commitmentLevel = CommitmentLevel.DEVICE_LOCK,
                    defaultGoal = "Restorative Sleep",
                    escalationEnabled = false,
                    profileName = "Social Detox",
                    isDefault = true
                ),
                SmartPreset(
                    id = 5,
                    name = "Social Detox",
                    emoji = "🚫",
                    description = "Break infinite feed dopamine addiction",
                    durationMinutes = 240,
                    commitmentLevel = CommitmentLevel.HARDCORE,
                    defaultGoal = "Break Social Feed Addiction",
                    escalationEnabled = true,
                    escalationAttemptTrigger = 3,
                    profileName = "Social Detox",
                    isDefault = true
                )
            )
        }
    }
}
