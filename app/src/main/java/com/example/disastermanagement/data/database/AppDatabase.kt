package com.example.disastermanagement.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.disastermanagement.data.ChatMessage
import com.example.disastermanagement.data.StringListConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.first

@Database(entities = [Incident::class, User::class, ChatMessage::class, AuditLog::class], version = 10, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun userDao(): UserDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "disaster_management_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            Executors.newSingleThreadExecutor().execute {
                                CoroutineScope(Dispatchers.IO).launch {
                                    prepopulate(database.userDao())
                                }
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun prepopulate(userDao: UserDao) {
            val admins = userDao.getUsersByRoleList("admin").first()
            if (admins.isEmpty()) {
                userDao.insertUser(User(email = "admin@test.com", fullName = "Admin User", password = "password123", role = "admin"))
            }
            val barangays = userDao.getUsersByRoleList("barangay").first()
            if (barangays.isEmpty()) {
                userDao.insertUser(User(email = "barangay@test.com", fullName = "Barangay User", password = "password123", role = "barangay"))
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE incidents ADD COLUMN status TEXT NOT NULL DEFAULT 'Reported'")
                database.execSQL("ALTER TABLE incidents ADD COLUMN severity TEXT NOT NULL DEFAULT 'Low'")
                database.execSQL("ALTER TABLE incidents ADD COLUMN isResolved INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `role` TEXT NOT NULL)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN password TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN fullName TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN pfpUrl TEXT NOT NULL DEFAULT ''")
            }
        }
        
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`messageId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `message` TEXT NOT NULL, `senderId` TEXT NOT NULL, `senderName` TEXT NOT NULL, `senderPfpUrl` TEXT NOT NULL, `senderRole` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `likes` INTEGER NOT NULL, `dislikes` INTEGER NOT NULL, `replies` TEXT NOT NULL)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN likedBy TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN dislikedBy TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN parentId INTEGER")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `audit_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `actorId` TEXT NOT NULL, `actorEmail` TEXT NOT NULL, `actionType` TEXT NOT NULL, `targetType` TEXT NOT NULL, `targetId` TEXT NOT NULL, `details` TEXT NOT NULL)")
            }
        }
    }
}
