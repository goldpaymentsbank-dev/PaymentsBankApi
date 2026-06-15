package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity

@Database(entities = [AccountEntity::class, TransactionEntity::class], version = 1, exportSchema = false)
abstract class BankingDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: BankingDatabase? = null

        fun getDatabase(context: Context): BankingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BankingDatabase::class.java,
                    "gold_payments_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
