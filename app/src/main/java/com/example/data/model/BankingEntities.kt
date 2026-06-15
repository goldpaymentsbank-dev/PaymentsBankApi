package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val username: String,
    val passwordHash: String, // passwords will be validated locally
    val fullName: String,
    val accountNumber: String,
    val balance: Double
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String, // owner of the transaction
    val type: String, // "Enviado" (Sent), "Recibido" (Received), "Depósito" (Deposit)
    val destinationAccount: String,
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
