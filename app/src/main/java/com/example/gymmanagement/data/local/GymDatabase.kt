package com.example.gymmanagement.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gymmanagement.data.local.dao.MemberDao
import com.example.gymmanagement.data.local.dao.PlanDao
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.entity.Plan

@Database(
    entities = [Member::class, Plan::class],
    version = 1,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun planDao(): PlanDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getInstance(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_database"
                )
                    // Use fallbackToDestructiveMigration for now to keep setup simple.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
