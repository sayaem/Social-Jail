package com.example.domain.model

import com.example.data.local.entity.ProfileEntity

data class Profile(
    val id: Long = 0,
    val name: String,
    val iconEmoji: String = "🧠",
    val packageNames: List<String>,
    val defaultDurationMinutes: Int = 120,
    val isPredefined: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): ProfileEntity {
        return ProfileEntity(
            id = id,
            name = name,
            iconEmoji = iconEmoji,
            packageNames = packageNames.joinToString(","),
            defaultDurationMinutes = defaultDurationMinutes,
            isPredefined = isPredefined,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromEntity(entity: ProfileEntity): Profile {
            val pkgs = if (entity.packageNames.isBlank()) emptyList() else entity.packageNames.split(",")
            return Profile(
                id = entity.id,
                name = entity.name,
                iconEmoji = entity.iconEmoji,
                packageNames = pkgs,
                defaultDurationMinutes = entity.defaultDurationMinutes,
                isPredefined = entity.isPredefined,
                createdAt = entity.createdAt
            )
        }

        fun getDefaultPresets(): List<Profile> {
            return listOf(
                Profile(
                    name = "Study",
                    iconEmoji = "📚",
                    packageNames = listOf(
                        "com.instagram.android",
                        "com.zhiliaoapp.musically",
                        "com.google.android.youtube",
                        "com.reddit.frontpage",
                        "com.facebook.katana",
                        "com.twitter.android"
                    ),
                    defaultDurationMinutes = 120,
                    isPredefined = true
                ),
                Profile(
                    name = "Deep Work",
                    iconEmoji = "💻",
                    packageNames = listOf(
                        "com.instagram.android",
                        "com.zhiliaoapp.musically",
                        "com.google.android.youtube",
                        "com.reddit.frontpage",
                        "com.facebook.katana",
                        "com.twitter.android",
                        "com.snapchat.android",
                        "com.netflix.mediaclient"
                    ),
                    defaultDurationMinutes = 180,
                    isPredefined = true
                ),
                Profile(
                    name = "Sleep",
                    iconEmoji = "😴",
                    packageNames = listOf(
                        "com.instagram.android",
                        "com.zhiliaoapp.musically",
                        "com.google.android.youtube",
                        "com.reddit.frontpage",
                        "com.facebook.katana",
                        "com.netflix.mediaclient",
                        "com.amazon.avod.thirdpartyclient"
                    ),
                    defaultDurationMinutes = 480,
                    isPredefined = true
                ),
                Profile(
                    name = "Custom",
                    iconEmoji = "🧠",
                    packageNames = emptyList(),
                    defaultDurationMinutes = 60,
                    isPredefined = true
                )
            )
        }
    }
}
