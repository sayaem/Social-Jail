package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BlockAttemptDao
import com.example.data.local.dao.DeviceLockSessionDao
import com.example.data.local.dao.ExamPlanDao
import com.example.data.local.dao.LockSessionDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.SmartPresetDao
import com.example.data.local.entity.BlockAttemptEntity
import com.example.data.local.entity.DeviceLockSessionEntity
import com.example.data.local.entity.ExamPlanEntity
import com.example.data.local.entity.LockSessionEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.SmartPresetEntity

@Database(
    entities = [
        LockSessionEntity::class,
        DeviceLockSessionEntity::class,
        ProfileEntity::class,
        ScheduleEntity::class,
        BlockAttemptEntity::class,
        ExamPlanEntity::class,
        SmartPresetEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lockSessionDao(): LockSessionDao
    abstract fun deviceLockSessionDao(): DeviceLockSessionDao
    abstract fun profileDao(): ProfileDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun blockAttemptDao(): BlockAttemptDao
    abstract fun examPlanDao(): ExamPlanDao
    abstract fun smartPresetDao(): SmartPresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "social_jail_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
