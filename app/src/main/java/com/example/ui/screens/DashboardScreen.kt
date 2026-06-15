package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.BankingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BankingViewModel,
    onNavigateToTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account by viewModel.currentAccount.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    // Filter to last 3 transactions for a cleaner dashboard (as requested)
    val lastThreeTransactions = remember(transactions) {
        transactions.take(3)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "GOLD PAYMENTS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToTransfer,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = { Text("Nueva Transferencia", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("fab_transfer")
                    .padding(bottom = 8.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Welcome user header
                account?.let { act ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Bienvenido de vuelta,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = act.fullName,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                // Premium Card rendering
                account?.let { act ->
                    PremiumCreditCard(account = act)
                }
            }

            // Centralized Quick Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionButton(
                        icon = Icons.AutoMirrored.Filled.Send,
                        label = "Enviar",
                        onClick = onNavigateToTransfer,
                        color = MaterialTheme.colorScheme.primary
                    )
                    QuickActionButton(
                        icon = Icons.Default.AccountBalance,
                        label = "Cuentas",
                        onClick = {},
                        enabled = false,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.Security,
                        label = "Seguridad",
                        onClick = {},
                        enabled = false,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Transaction Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Últimos Movimientos (Máx 3)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Historial",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToTransfer() }
                    )
                }
            }

            // History elements
            if (lastThreeTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sin transacciones recientes",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tus movimientos de efectivo aparecerán aquí.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(lastThreeTransactions) { tx ->
                    TransactionItemRow(transaction = tx)
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp)) // padding for FAB and safe drawing
            }
        }
    }
}

@Composable
fun PremiumCreditCard(account: AccountEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF282830), // rich metallic carbon gray
                            Color(0xFF151518)  // deep obsidian black
                        )
                    )
                )
        ) {
            // Shiny visual design details (background abstract circles)
            Canvas(modifier = Modifier.fillMaxSize()) {
                clipRect {
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.08f),
                        radius = 200.dp.toPx(),
                        center = center.copy(x = center.x + 120.dp.toPx(), y = center.y - 40.dp.toPx())
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row with chip and logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gold Chip Illustration
                    Box(
                        modifier = Modifier
                            .size(38.dp, 28.dp)
                            .background(
                                color = GoldPrimary.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(Color.Black.copy(0.15f), start = center.copy(x = 0f), end = center.copy(x = size.width))
                            drawLine(Color.Black.copy(0.15f), start = center.copy(y = 0f), end = center.copy(y = size.height))
                        }
                    }

                    // Gold Card Branding
                    Text(
                        text = "GOLD PREMIUM",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = GoldPrimary
                    )
                }

                // Middle area displaying physical account and balance
                Column {
                    Text(
                        text = "Saldo de la Cuenta",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$${String.format("%,.2f", account.balance)} USD",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = LightTextPrimary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }

                // Balance account details footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NÚMERO DE CUENTA",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightTextSecondary,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = account.accountNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = LightTextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "Contactless payment active",
                        tint = GoldPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else LightTextSecondary
        )
    }
}

@Composable
fun TransactionItemRow(transaction: TransactionEntity) {
    val isDeposit = transaction.type == "Depósito" || transaction.type == "Recibido"
    val dateString = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "MX"))
        sdf.format(Date(transaction.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circle visual indicator containing vector icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isDeposit) AccentSuccess.copy(alpha = 0.15f) else AccentDanger.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isDeposit) AccentSuccess.copy(alpha = 0.3f) else AccentDanger.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDeposit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = if (isDeposit) "Depósito" else "Transferencia Enviada",
                        tint = if (isDeposit) AccentSuccess else AccentDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isDeposit) "Recibido • $dateString" else "Transferencia a ${transaction.destinationAccount} • $dateString",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Text(
                text = "${if (isDeposit) "+" else "-"}$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (isDeposit) AccentSuccess else LightTextPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
