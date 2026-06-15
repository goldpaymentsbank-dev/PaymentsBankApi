package com.example.data.local

import androidx.room.*
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE username = :username LIMIT 1")
    fun getAccountFlow(username: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE username = :username LIMIT 1")
    suspend fun getAccount(username: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = :newBalance WHERE username = :username")
    suspend fun updateBalance(username: String, newBalance: Double)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE username = :username ORDER BY timestamp DESC")
    fun getTransactionsFlow(username: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}
