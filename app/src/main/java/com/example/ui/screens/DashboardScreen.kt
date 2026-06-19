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

    var showBottomSheet by remember { mutableStateOf(false) }
    val quickTransferStatus by viewModel.quickTransferStatus.collectAsState()

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

            item {
                account?.let { act ->
                    BalanceTrendChart(currentBalance = act.balance, transactions = transactions)
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
                        icon = Icons.Default.Bolt,
                        label = "Rápido",
                        onClick = { showBottomSheet = true },
                        color = MaterialTheme.colorScheme.tertiary
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

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                viewModel.resetQuickTransferStatus()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            QuickTransferBottomSheetContent(
                currentAccount = account,
                status = quickTransferStatus,
                onTransfer = { destination, amount, name ->
                    viewModel.performQuickTransfer(destination, amount, name)
                },
                onClose = {
                    showBottomSheet = false
                    viewModel.resetQuickTransferStatus()
                }
            )
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

// Predefined list of contacts for rapid transfers
data class PredefinedContact(
    val name: String,
    val accountNumber: String,
    val initials: String,
    val avatarBg: Color
)

@Composable
fun BalanceTrendChart(currentBalance: Double, transactions: List<TransactionEntity>) {
    // Generate deterministic 30-day balance points working backwards
    val points = remember(currentBalance, transactions) {
        val days = 30
        val history = DoubleArray(days)
        var balance = currentBalance
        history[days - 1] = balance
        
        // Sort transactions by descending date to work backward
        val sortedTxs = transactions.sortedByDescending { it.timestamp }
        
        // Let's map days to balances
        var txIndex = 0
        val now = System.currentTimeMillis()
        
        // We'll calculate balance day by day backwards
        for (i in (days - 2) downTo 0) {
            val dayStart = now - (days - 1 - i) * 86400000L
            val dayEnd = now - (days - 2 - i) * 86400000L
            
            // Check if any transaction occurred during this day range
            var delta = 0.0
            while (txIndex < sortedTxs.size && sortedTxs[txIndex].timestamp in (dayStart..dayEnd)) {
                val tx = sortedTxs[txIndex]
                val isDeposit = tx.type == "Depósito" || tx.type == "Recibido"
                delta += if (isDeposit) -tx.amount else tx.amount
                txIndex++
            }
            balance += delta
            // Keep balance within sensible positive range for visualization
            history[i] = balance.coerceAtLeast(100.0)
        }
        
        // If there are no historical transitions, let's seed a nice realistic wavy curve working backward
        if (transactions.size <= 3) {
            var tempBalance = currentBalance
            for (i in (days - 1) downTo 0) {
                val wave = Math.sin(i * 0.5) * 150.0
                val trend = (i - days) * 12.0
                history[i] = (tempBalance + wave + trend).coerceAtLeast(300.0)
            }
            history[days - 1] = currentBalance
        }
        history.toList()
    }
    
    val maxVal = remember(points) { points.maxOrNull()?.toFloat() ?: 1000f }
    val minVal = remember(points) { points.minOrNull()?.toFloat() ?: 100f }
    val range = remember(maxVal, minVal) { (maxVal - minVal).coerceAtLeast(1f) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tendencia de Saldo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Evolución de cuenta en últimos 30 días",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF2E6342).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, Color(0xFF2E6342).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "USD",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1AECC1)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1)
            
            val gridLines = 3
            for (g in 0..gridLines) {
                val fraction = g.toFloat() / gridLines
                val y = height * (1f - fraction)
                
                drawLine(
                    color = Color.Gray.copy(alpha = 0.08f),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1f
                )
            }
            
            val pathPoints = points.mapIndexed { idx, value ->
                val x = idx * stepX
                val normalizedY = (value.toFloat() - minVal) / range
                val paddingPx = 10.dp.toPx()
                val y = paddingPx + (height - paddingPx * 2) * (1f - normalizedY)
                androidx.compose.ui.geometry.Offset(x, y)
            }
            
            val strokePath = androidx.compose.ui.graphics.Path().apply {
                if (pathPoints.isNotEmpty()) {
                    moveTo(pathPoints[0].x, pathPoints[0].y)
                    for (i in 1 until pathPoints.size) {
                        val prev = pathPoints[i - 1]
                        val current = pathPoints[i]
                        val controlX = (prev.x + current.x) / 2
                        cubicTo(controlX, prev.y, controlX, current.y, current.x, current.y)
                    }
                }
            }
            
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                if (pathPoints.isNotEmpty()) {
                    moveTo(0f, height)
                    lineTo(pathPoints[0].x, pathPoints[0].y)
                    for (i in 1 until pathPoints.size) {
                        val prev = pathPoints[i - 1]
                        val current = pathPoints[i]
                        val controlX = (prev.x + current.x) / 2
                        cubicTo(controlX, prev.y, controlX, current.y, current.x, current.y)
                    }
                    lineTo(width, height)
                    close()
                }
            }
            
            val fillGradient = Brush.verticalGradient(
                colors = listOf(
                    GoldPrimary.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
            
            val strokeGradient = Brush.horizontalGradient(
                colors = listOf(
                    GoldPrimary.copy(0.6f),
                    GoldPrimary,
                    Color(0xFFE2B755)
                )
            )
            
            drawPath(
                path = fillPath,
                brush = fillGradient
            )
            
            drawPath(
                path = strokePath,
                brush = strokeGradient,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
            
            if (pathPoints.isNotEmpty()) {
                val finalDot = pathPoints.last()
                drawCircle(
                    color = GoldPrimary.copy(alpha = 0.25f),
                    radius = 8.dp.toPx(),
                    center = finalDot
                )
                drawCircle(
                    color = GoldPrimary,
                    radius = 4.dp.toPx(),
                    center = finalDot
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hace 30 días",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = LightTextSecondary
            )
            Text(
                text = "Mín: $${String.format("%,.0f", minVal)} • Máx: $${String.format("%,.0f", maxVal)}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Text(
                text = "Hoy",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun QuickTransferBottomSheetContent(
    currentAccount: AccountEntity?,
    status: com.example.ui.viewmodel.QuickTransferStatus,
    onTransfer: (destinationAccountNumber: String, amount: Double, recipientName: String) -> Unit,
    onClose: () -> Unit
) {
    var selectedContact by remember { mutableStateOf<PredefinedContact?>(null) }
    var transferAmountStr by remember { mutableStateOf("") }
    
    val predefinedContacts = remember {
        listOf(
            PredefinedContact("Sofía Rodríguez", "MX-987654", "SR", Color(0xFFE53935)),
            PredefinedContact("Carlos Mendoza", "MX-123456", "CM", Color(0xFF1E88E5)),
            PredefinedContact("María Fernández", "MX-888999", "MF", Color(0xFF8E24AA))
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (status) {
            is com.example.ui.viewmodel.QuickTransferStatus.Success -> {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(AccentSuccess.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        tint = AccentSuccess,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "¡Traspaso Exitoso!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = status.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
                
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Entendido", fontWeight = FontWeight.Bold)
                }
            }
            else -> {
                Text(
                    text = "Traspaso Rápido de Fondos",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Seleccione un contacto para realizar una transferencia inmediata",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )
                
                // Show Error Banner inside the sheet
                if (status is com.example.ui.viewmodel.QuickTransferStatus.Error) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = status.errorMsg,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                // Contact List Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    predefinedContacts.forEach { contact ->
                        val isSelected = selectedContact == contact
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedContact = contact }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                                .width(80.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(contact.avatarBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.initials,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = contact.name.split(" ")[0],
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = contact.accountNumber,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                                color = LightTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                if (selectedContact != null) {
                    val contact = selectedContact!!
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 20.dp))
                    
                    Text(
                        text = "Enviar a ${contact.name}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = transferAmountStr,
                        onValueChange = { transferAmountStr = it },
                        label = { Text("Monto a enviar (USD)") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    
                    // Quick amount shortcuts row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(20, 50, 100, 200).forEach { amt ->
                            SuggestionChip(
                                onClick = { transferAmountStr = amt.toString() },
                                label = { Text("+$amt") },
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val isLoading = status is com.example.ui.viewmodel.QuickTransferStatus.Loading
                    Button(
                        onClick = {
                            val amt = transferAmountStr.toDoubleOrNull() ?: 0.0
                            onTransfer(contact.accountNumber, amt, contact.name)
                        },
                        enabled = !isLoading && transferAmountStr.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Confirmar Traspaso de Rango", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Por favor, seleccione un destinatario arriba.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightTextSecondary
                        )
                    }
                }
            }
        }
    }
}
