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

sealed class QuickTransferStatus {
    object Idle : QuickTransferStatus()
    object Loading : QuickTransferStatus()
    data class Success(val message: String) : QuickTransferStatus()
    data class Error(val errorMsg: String) : QuickTransferStatus()
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

    // Register
    val registerUsername = MutableStateFlow("")
    val registerPassword = MutableStateFlow("")
    val registerFullName = MutableStateFlow("")
    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()
    private val _registerSuccess = MutableStateFlow<String?>(null)
    val registerSuccess: StateFlow<String?> = _registerSuccess.asStateFlow()

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

    fun registerUser() {
        _registerError.value = null
        _registerSuccess.value = null
        val username = registerUsername.value.trim().lowercase()
        val password = registerPassword.value
        val fullName = registerFullName.value.trim()

        if (username.isBlank() || password.isBlank() || fullName.isBlank()) {
            _registerError.value = "Por favor ingrese todos los campos."
            return
        }

        viewModelScope.launch {
            try {
                val existing = repository.getAccount(username)
                if (existing != null) {
                    _registerError.value = "El nombre de usuario '$username' ya se encuentra registrado."
                    return@launch
                }

                // STP (Sistema de Transferencias y Pagos) CLABE format in Mexico:
                // 3 digits bank (646) + 3 digits city (180) + 11 digits account/phone + 1 checksum digit = 18 digits.
                val builder = java.lang.StringBuilder("646180")
                repeat(12) {
                    builder.append((0..9).random())
                }
                val generatedCLABE = builder.toString()

                val bonusAmount = 100000.00
                val newAccount = AccountEntity(
                    username = username,
                    passwordHash = password,
                    fullName = fullName,
                    accountNumber = generatedCLABE,
                    balance = bonusAmount
                )

                // Save to Room database
                repository.createAccount(newAccount)

                // Log the welcome registration bonus as an initial transaction deposit!
                val bonusTx = TransactionEntity(
                    username = username,
                    type = "Depósito",
                    destinationAccount = generatedCLABE,
                    amount = bonusAmount,
                    description = "Bono de Inscripción Gold Premium STP"
                )
                repository.insertTransaction(bonusTx)

                _registerSuccess.value = "¡Registro Exitoso! Se te ha asignado tu Cuenta STP Clabe: $generatedCLABE con un Bono de Bienvenida de $100,000.00 USD."
                
                // Clear status flows
                registerUsername.value = ""
                registerPassword.value = ""
                registerFullName.value = ""
            } catch (e: Exception) {
                _registerError.value = "Ocurrió un error inesperado al registrar el usuario: ${e.message}"
            }
        }
    }

    fun clearRegisterErrorAndSuccess() {
        _registerError.value = null
        _registerSuccess.value = null
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

    // State for Quick Transfer Bottom Sheet
    private val _quickTransferStatus = MutableStateFlow<QuickTransferStatus>(QuickTransferStatus.Idle)
    val quickTransferStatus: StateFlow<QuickTransferStatus> = _quickTransferStatus.asStateFlow()

    fun performQuickTransfer(destination: String, amount: Double, recipientName: String) {
        val sender = _currentUser.value ?: return
        if (amount <= 0) {
            _quickTransferStatus.value = QuickTransferStatus.Error("El monto debe ser mayor que cero.")
            return
        }
        _quickTransferStatus.value = QuickTransferStatus.Loading
        viewModelScope.launch {
            val result = repository.executeTransfer(
                senderUsername = sender,
                destinationAccountNumber = destination,
                amount = amount,
                description = "Traspaso rápido a $recipientName"
            )
            when (result) {
                is TransferResult.Success -> {
                    _quickTransferStatus.value = QuickTransferStatus.Success(result.message)
                }
                is TransferResult.Error -> {
                    _quickTransferStatus.value = QuickTransferStatus.Error(result.errorMsg)
                }
            }
        }
    }

    fun resetQuickTransferStatus() {
        _quickTransferStatus.value = QuickTransferStatus.Idle
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
