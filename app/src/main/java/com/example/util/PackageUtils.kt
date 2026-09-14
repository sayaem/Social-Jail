package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.domain.model.AppCategory
import com.example.domain.model.InstalledAppInfo

object PackageUtils {

    private val SOCIAL_PACKAGES = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.lite",
        "com.facebook.orca",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.twitter.android",
        "com.reddit.frontpage",
        "com.snapchat.android",
        "com.pinterest",
        "com.discord",
        "com.linkedin.android",
        "com.tumblr"
    )

    private val VIDEO_PACKAGES = setOf(
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.netflix.mediaclient",
        "tv.twitch.android.app",
        "com.disney.disneyplus",
        "com.hulu.plus",
        "com.amazon.avod.thirdpartyclient",
        "com.spotify.music"
    )

    private val BROWSER_PACKAGES = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.brave.browser",
        "com.sec.android.app.sbrowser",
        "com.microsoft.emmx",
        "com.opera.browser",
        "com.duckduckgo.mobile.android"
    )

    private val ESSENTIAL_PREFIXES = setOf(
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.phone",
        "com.google.android.contacts",
        "com.android.contacts",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.google.android.calculator",
        "com.android.calculator2",
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.google.android.calendar",
        "com.android.calendar",
        "com.google.android.apps.classroom",
        "us.zoom.videomeetings",
        "com.google.android.apps.meetings",
        "com.microsoft.teams"
    )

    fun isEssentialApp(packageName: String, contextPackageName: String): Boolean {
        if (packageName == contextPackageName) return true
        if (packageName.contains("dialer", ignoreCase = true) ||
            packageName.contains("emergency", ignoreCase = true) ||
            packageName.contains("telecom", ignoreCase = true) ||
            packageName.contains("telephony", ignoreCase = true) ||
            packageName.contains("incallui", ignoreCase = true)
        ) {
            return true
        }
        return ESSENTIAL_PREFIXES.any { packageName.startsWith(it, ignoreCase = true) }
    }

    fun getCategoryForPackage(packageName: String, appInfo: ApplicationInfo?): AppCategory {
        if (SOCIAL_PACKAGES.contains(packageName)) return AppCategory.SOCIAL
        if (VIDEO_PACKAGES.contains(packageName)) return AppCategory.VIDEO
        if (BROWSER_PACKAGES.contains(packageName)) return AppCategory.BROWSERS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) return AppCategory.GAMES
            if (appInfo.category == ApplicationInfo.CATEGORY_SOCIAL) return AppCategory.SOCIAL
            if (appInfo.category == ApplicationInfo.CATEGORY_VIDEO) return AppCategory.VIDEO
        }
        return AppCategory.OTHER
    }

    fun getInstalledApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val seenPackages = mutableSetOf<String>()
        val result = mutableListOf<InstalledAppInfo>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) continue // Never block Social Jail itself
            if (seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val appName = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            val isEssential = isEssentialApp(pkg, context.packageName)
            val appInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getApplicationInfo(pkg, 0)
                }
            } catch (e: Exception) {
                null
            }
            val category = getCategoryForPackage(pkg, appInfo)

            result.add(
                InstalledAppInfo(
                    packageName = pkg,
                    appName = appName,
                    icon = icon,
                    category = category,
                    isEssential = isEssential
                )
            )
        }

        // If running in restricted emulator or test environment with very few launcher apps,
        // provide popular demo apps so the user can immediately test Social Jail's locking functionality.
        if (result.count { !it.isEssential } < 3) {
            val demoPopular = listOf(
                InstalledAppInfo("com.instagram.android", "Instagram", null, AppCategory.SOCIAL, false),
                InstalledAppInfo("com.facebook.katana", "Facebook", null, AppCategory.SOCIAL, false),
                InstalledAppInfo("com.zhiliaoapp.musically", "TikTok", null, AppCategory.SOCIAL, false),
                InstalledAppInfo("com.google.android.youtube", "YouTube", null, AppCategory.VIDEO, false),
                InstalledAppInfo("com.reddit.frontpage", "Reddit", null, AppCategory.SOCIAL, false),
                InstalledAppInfo("com.twitter.android", "X (Twitter)", null, AppCategory.SOCIAL, false),
                InstalledAppInfo("com.android.chrome", "Chrome", null, AppCategory.BROWSERS, false),
                InstalledAppInfo("com.netflix.mediaclient", "Netflix", null, AppCategory.VIDEO, false)
            )
            for (demo in demoPopular) {
                if (!seenPackages.contains(demo.packageName)) {
                    result.add(demo)
                }
            }
        }

        return result.sortedWith(
            compareBy<InstalledAppInfo> { it.isEssential }
                .thenBy { it.appName.lowercase() }
        )
    }
}
