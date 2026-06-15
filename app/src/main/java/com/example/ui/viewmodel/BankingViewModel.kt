package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.BankingRepository
import com.example.data.repository.TransferResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val account: AccountEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}

class BankingViewModel(private val repository: BankingRepository) : ViewModel() {

    // Auth states
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Logged in user session
    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()

    // Account state (live updates)
    val currentAccount: StateFlow<AccountEntity?> = _currentUser
        .flatMapLatest { username ->
            if (username != null) {
                repository.getAccountFlow(username)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Transactions list (live updates)
    val transactions: StateFlow<List<TransactionEntity>> = _currentUser
        .flatMapLatest { username ->
            if (username != null) {
                repository.getTransactionsFlow(username)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form states
    // Login
    val loginUsername = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Transfer
    val transferDestination = MutableStateFlow("")
    val transferAmount = MutableStateFlow("")
    val transferDescription = MutableStateFlow("")
    
    private val _transferLoading = MutableStateFlow(false)
    val transferLoading: StateFlow<Boolean> = _transferLoading.asStateFlow()

    private val _transferSuccessMessage = MutableStateFlow<String?>(null)
    val transferSuccessMessage: StateFlow<String?> = _transferSuccessMessage.asStateFlow()

    private val _transferErrorMessage = MutableStateFlow<String?>(null)
    val transferErrorMessage: StateFlow<String?> = _transferErrorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database at launch with Alexander Gold (admin / 1234)
            repository.preseedDatabaseIfEmpty()
        }
    }

    fun login() {
        _loginError.value = null
        val username = loginUsername.value.trim().lowercase()
        val password = loginPassword.value

        if (username.isBlank() || password.isBlank()) {
            _loginError.value = "Por favor ingrese usuario y contraseña."
            return
        }

        viewModelScope.launch {
            val account = repository.getAccount(username)
            if (account != null && account.passwordHash == password) {
                completeLogin(account)
            } else {
                _loginError.value = "Credenciales incorrectas (Prueba con admin / 1234)."
            }
        }
    }

    fun biometricLogin() {
        _loginError.value = null
        viewModelScope.launch {
            // For demo purposes, biometric logs in as admin
            val account = repository.getAccount("admin")
            if (account != null) {
                completeLogin(account)
            } else {
                _loginError.value = "Error en autenticación biométrica: Cuenta no encontrada."
            }
        }
    }

    fun onBiometricError(error: String) {
        _loginError.value = error
    }

    private fun completeLogin(account: AccountEntity) {
        _currentUser.value = account.username
        _authState.value = AuthState.Authenticated(account)
        // Clear fields on successful login
        loginPassword.value = ""
        loginUsername.value = ""
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        loginUsername.value = ""
        loginPassword.value = ""
        clearTransferForm()
    }

    fun performTransfer() {
        val sender = _currentUser.value ?: return
        val destination = transferDestination.value.trim()
        val amountStr = transferAmount.value.trim()
        val desc = transferDescription.value.trim()

        _transferSuccessMessage.value = null
        _transferErrorMessage.value = null

        if (destination.isBlank()) {
            _transferErrorMessage.value = "Por favor ingrese la cuenta destino."
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _transferErrorMessage.value = "Por favor ingrese un monto válido mayor a 0."
            return
        }

        _transferLoading.value = true
        viewModelScope.launch {
            val result = repository.executeTransfer(
                senderUsername = sender,
                destinationAccountNumber = destination,
                amount = amount,
                description = desc
            )
            _transferLoading.value = false
            when (result) {
                is TransferResult.Success -> {
                    _transferSuccessMessage.value = result.message
                    clearTransferForm()
                }
                is TransferResult.Error -> {
                    _transferErrorMessage.value = result.errorMsg
                }
            }
        }
    }

    fun selectQuickRecipient(accountNumber: String) {
        transferDestination.value = accountNumber
    }

    fun clearTransferForm() {
        transferDestination.value = ""
        transferAmount.value = ""
        transferDescription.value = ""
    }

    fun clearNotifications() {
        _transferSuccessMessage.value = null
        _transferErrorMessage.value = null
    }
}

class BankingViewModelFactory(private val repository: BankingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BankingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
