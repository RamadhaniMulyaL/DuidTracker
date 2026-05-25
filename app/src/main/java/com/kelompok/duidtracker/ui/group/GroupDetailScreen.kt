package com.kelompok.duidtracker.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok.duidtracker.data.local.entity.toFormattedBudget
import com.kelompok.duidtracker.ui.components.BalanceSummaryCard
import com.kelompok.duidtracker.ui.components.TransactionCard
import com.kelompok.duidtracker.ui.personal.AddTransactionDialog
import com.kelompok.duidtracker.viewmodel.GroupViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Layar Detail Grup.
 * Update: Tambah SplitBillDialog dan GroupSettingsDialog (Step F5.3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: GroupViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }  // BARU
    var showSplitBillDialog by remember { mutableStateOf(false) } // BARU

    // Prefill untuk AddTransactionDialog dari hasil split bill
    var splitBillPrefill by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(groupId) {
        viewModel.loadGroupDetail(groupId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(uiState.group?.name ?: "Detail Grup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (uiState.group != null) {
                            Text("Kode: ${uiState.group?.inviteCode}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Tombol Split Bill — selalu tampil
                    IconButton(onClick = { showSplitBillDialog = true }) {
                        Icon(Icons.Default.Calculate, contentDescription = "Split Bill")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        // Settings hanya untuk owner
                        if (uiState.group?.ownerId == uiState.currentUserId) {
                            DropdownMenuItem(
                                text = { Text("Pengaturan Grup") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showSettingsDialog = true
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Keluar Grup") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showLeaveConfirm = true
                            }
                        )
                        if (uiState.group?.ownerId == uiState.currentUserId) {
                            DropdownMenuItem(
                                text = { Text("Hapus Grup", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi Grup")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            BalanceSummaryCard(
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                balance = uiState.balance
            )

            uiState.group?.let { group ->
                // Budget progress card
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF1565C0))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Anggaran Grup", fontSize = 12.sp, color = Color.Gray)
                                Text(group.toFormattedBudget, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            VerticalDivider(modifier = Modifier.height(30.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Anggota", fontSize = 12.sp, color = Color.Gray)
                                Text("${group.getMembersList().size} Orang", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Budget progress bar
                        if (group.budget > 0) {
                            val progress = (uiState.totalExpense / group.budget).toFloat().coerceIn(0f, 1f)
                            val progressColor = when {
                                progress < 0.7f -> Color(0xFF4CAF50)
                                progress < 0.9f -> Color(0xFFFFA000)
                                else -> Color(0xFFF44336)
                            }
                            val formatter = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Budget terpakai", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    "Rp ${formatter.format(uiState.totalExpense)} / ${group.toFormattedBudget}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = progressColor,
                                trackColor = progressColor.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Transaksi Grup",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi di grup ini.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.transactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onDelete = { viewModel.deleteGroupTransaction(it) }
                        )
                    }
                }
            }
        }
    }

    // Dialog tambah transaksi — bisa prefill dari split bill
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = {
                showAddDialog = false
                splitBillPrefill = null
            },
            onConfirm = { nama, nominal, kategori, type, tanggal ->
                viewModel.addGroupTransaction(groupId, nama, nominal, kategori, type, tanggal)
                showAddDialog = false
                splitBillPrefill = null
            },
            prefillNominal = splitBillPrefill
        )
    }

    // Konfirmasi keluar grup
    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text("Keluar Grup") },
            text = { Text("Apakah Anda yakin ingin keluar dari grup ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.leaveGroup(groupId)
                        showLeaveConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Keluar") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) { Text("Batal") }
            }
        )
    }

    // Konfirmasi hapus grup
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Grup") },
            text = { Text("Tindakan ini tidak dapat dibatalkan. Seluruh data grup akan dihapus.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(groupId)
                        showDeleteConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
            }
        )
    }

    // GroupSettingsDialog — hanya untuk owner
    if (showSettingsDialog) {
        GroupSettingsDialog(
            currentBudget = uiState.group?.budget ?: 0.0,
            onDismiss = { showSettingsDialog = false },
            onResetBudget = { newBudget ->
                viewModel.resetBudget(groupId, newBudget)
                showSettingsDialog = false
            },
            onDeleteGroup = {
                showSettingsDialog = false
                showDeleteConfirm = true
            }
        )
    }

    // SplitBillDialog
    if (showSplitBillDialog) {
        SplitBillDialog(
            memberCount = uiState.group?.getMembersList()?.size ?: 1,
            onDismiss = { showSplitBillDialog = false },
            onCatatSebagaiExpense = { perOrang ->
                showSplitBillDialog = false
                splitBillPrefill = perOrang
                showAddDialog = true
            },
            calculateSplit = { total, people -> viewModel.calculateSplit(total, people) }
        )
    }
}

// ─────────────────────────────────────────────
// GroupSettingsDialog
// ─────────────────────────────────────────────

@Composable
fun GroupSettingsDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onResetBudget: (Double) -> Unit,
    onDeleteGroup: () -> Unit
) {
    var budgetInput by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toLong().toString() else "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Yakin hapus grup ini? Semua data akan hilang dan tidak bisa dikembalikan.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteGroup()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Hapus Permanen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan Grup", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Update Anggaran", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Budget Baru (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val newBudget = budgetInput.toDoubleOrNull() ?: return@Button
                        onResetBudget(newBudget)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    enabled = budgetInput.toDoubleOrNull() != null
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Budget")
                }

                HorizontalDivider()

                // Tombol hapus grup — merah, perlu konfirmasi
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Grup")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

// ─────────────────────────────────────────────
// SplitBillDialog
// ─────────────────────────────────────────────

@Composable
fun SplitBillDialog(
    memberCount: Int,
    onDismiss: () -> Unit,
    onCatatSebagaiExpense: (Double) -> Unit,
    calculateSplit: (Double, Int) -> Double
) {
    var totalInput by remember { mutableStateOf("") }
    var peopleInput by remember { mutableStateOf(memberCount.toString()) }

    val total = totalInput.toDoubleOrNull() ?: 0.0
    val people = peopleInput.toIntOrNull()?.coerceIn(1, 10) ?: 1
    val perOrang = calculateSplit(total, people)

    val formatter = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Split Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = totalInput,
                    onValueChange = { totalInput = it },
                    label = { Text("Total Tagihan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = peopleInput,
                    onValueChange = { if (it.toIntOrNull()?.let { n -> n in 1..10 } == true || it.isEmpty()) peopleInput = it },
                    label = { Text("Jumlah Orang (maks. 10)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (total > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Per orang:", fontSize = 14.sp, color = Color.Gray)
                            Text(
                                text = "Rp ${formatter.format(perOrang)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCatatSebagaiExpense(perOrang) },
                enabled = total > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Catat sebagai Expense Grup")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}