package com.example

/**
 * ==================================================================================
 * INSTRUCCIONES PARA GENERAR EL ARCHIVO APK COMPILADO EN ANDROID STUDIO:
 * 
 * 1. Abre este proyecto en Android Studio (asegúrate de que compile correctamente).
 * 2. En el menú superior, selecciona: 'Build' -> 'Build Bundle(s) / APK(s)' -> 'Build APK(s)'.
 * 3. Android Studio compilará el código y mostrará una notificación flotante abajo a la derecha.
 * 4. Haz clic en 'Locate' en la notificación para abrir la carpeta que contiene el archivo 'app-debug.apk'.
 * 5. Envía este archivo '.apk' a tu teléfono móvil e instálalo (permite la instalación de fuentes desconocidas).
 * ==================================================================================
 */

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.BankingDatabase
import com.example.data.repository.BankingRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.BankingViewModel
import com.example.ui.viewmodel.BankingViewModelFactory

class MainActivity : FragmentActivity() {

    // Lazy initialization of database and repository for optimal launch speed
    private val database by lazy { BankingDatabase.getDatabase(this) }
    private val repository by lazy {
        BankingRepository(
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge drawing
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Initialize ViewModel passing our constructor injected repository
                    val viewModel: BankingViewModel = viewModel(
                        factory = BankingViewModelFactory(repository)
                    )

                    MainSessionContainer(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainSessionContainer(
    viewModel: BankingViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()

    // Crossfade provides clean transition between login and logged-in app state
    Crossfade(
        targetState = authState,
        animationSpec = tween(durationMillis = 400),
        label = "SessionCrossfade"
    ) { currentAuth ->
        when (currentAuth) {
            is AuthState.Unauthenticated, is AuthState.Error -> {
                LoginScreen(
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
            is AuthState.Authenticated -> {
                AppNavigationStack(
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun AppNavigationStack(
    viewModel: BankingViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToTransfer = {
                    navController.navigate("transfer")
                }
            )
        }
        composable("transfer") {
            TransferScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
