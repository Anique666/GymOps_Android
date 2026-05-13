package com.example.gymmanagement.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymmanagement.data.local.dao.EquipmentDao
import com.example.gymmanagement.data.local.dao.MaintenanceDao
import com.example.gymmanagement.data.local.dao.PaymentDao
import com.example.gymmanagement.data.local.dao.ReportsDao
import com.example.gymmanagement.data.local.dao.MemberDao
import com.example.gymmanagement.data.local.dao.PlanDao
import com.example.gymmanagement.data.local.entity.EquipmentEntity
import com.example.gymmanagement.data.local.entity.MaintenanceEntity
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.entity.PaymentEntity
import com.example.gymmanagement.data.local.entity.Plan

@Database(
    entities = [
        Member::class,
        Plan::class,
        PaymentEntity::class,
        EquipmentEntity::class,
        MaintenanceEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(GymTypeConverters::class)
abstract class GymDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun planDao(): PlanDao
    abstract fun paymentDao(): PaymentDao
    abstract fun reportsDao(): ReportsDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun maintenanceDao(): MaintenanceDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                DemoDataSeeder.seedIfNeeded(instance)
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE members ADD COLUMN gender TEXT NOT NULL DEFAULT 'UNSPECIFIED'")
                database.execSQL("ALTER TABLE members ADD COLUMN dateOfBirth INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE members ADD COLUMN source TEXT NOT NULL DEFAULT 'Unknown'")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        memberId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        paymentDate INTEGER NOT NULL,
                        planId INTEGER NOT NULL,
                        isRenewal INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        FOREIGN KEY(memberId) REFERENCES members(id) ON UPDATE CASCADE ON DELETE CASCADE,
                        FOREIGN KEY(planId) REFERENCES plans(id) ON UPDATE CASCADE ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_payments_memberId ON payments(memberId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_payments_planId ON payments(planId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS equipment (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        serialNumber TEXT NOT NULL,
                        category TEXT NOT NULL,
                        status TEXT NOT NULL,
                        purchaseDate INTEGER NOT NULL,
                        lastServiceDate INTEGER NOT NULL,
                        usageHours INTEGER,
                        notes TEXT
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_equipment_serialNumber ON equipment(serialNumber)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_equipment_status ON equipment(status)")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS maintenance_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        equipmentId INTEGER NOT NULL,
                        issueDescription TEXT NOT NULL,
                        reportedDate INTEGER NOT NULL,
                        resolvedDate INTEGER,
                        status TEXT NOT NULL,
                        FOREIGN KEY(equipmentId) REFERENCES equipment(id) ON UPDATE CASCADE ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_maintenance_records_equipmentId ON maintenance_records(equipmentId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_maintenance_records_status ON maintenance_records(status)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE members ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE members ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE members ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE members ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE members SET updatedAt = strftime('%s','now')*1000 WHERE updatedAt = 0")

                database.execSQL("ALTER TABLE plans ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE plans ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE plans ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE plans ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE plans SET updatedAt = strftime('%s','now')*1000 WHERE updatedAt = 0")

                database.execSQL("ALTER TABLE payments ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE payments ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE payments ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE payments ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE payments SET updatedAt = strftime('%s','now')*1000 WHERE updatedAt = 0")

                database.execSQL("ALTER TABLE equipment ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE equipment ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE equipment ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE equipment ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE equipment SET updatedAt = strftime('%s','now')*1000 WHERE updatedAt = 0")

                database.execSQL("ALTER TABLE maintenance_records ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE maintenance_records ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE maintenance_records ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE maintenance_records ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE maintenance_records SET updatedAt = strftime('%s','now')*1000 WHERE updatedAt = 0")
            }
        }
    }
}
