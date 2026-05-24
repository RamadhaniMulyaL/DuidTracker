package com.kelompok.duidtracker.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok.duidtracker.data.local.entity.GroupEntity
import com.kelompok.duidtracker.data.local.entity.toFormattedBudget
import com.kelompok.duidtracker.viewmodel.GroupViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Layar Daftar Grup Keuangan.
 * Sesuai persyaratan Step F5.2:
 * - TopAppBar dengan aksi Join (QR) dan Create (+).
 * - GroupCard dengan icon emoji, member count, dan budget progress.
 * - Dialog pembuatan grup dengan pemilih emoji.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    viewModel: GroupViewModel,
    onGroupClick: (String) -> Unit
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grup Keuangan", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showJoinDialog = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Gabung Grup")
                    }
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Buat Grup")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else if (uiState.groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada grup. Buat atau gabung sekarang!",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.groups) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onGroupClick(group.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, budget, icon ->
                viewModel.createGroup(name, desc, budget, icon)
                showCreateDialog = false
            }
        )
    }

    if (showJoinDialog) {
        JoinGroupDialog(
            errorMessage = uiState.errorMessage,
            onDismiss = { showJoinDialog = false },
            onConfirm = { code ->
                viewModel.joinGroup(code)
            }
        )
    }
}

@Composable
fun GroupCard(group: GroupEntity, onClick: () -> Unit) {
    // Simulasi penggunaan saat ini (ideal diambil dari DB)
    val currentUsage = 0.0 
    val progress = if (group.budget > 0) (currentUsage / group.budget).toFloat() else 0f
    
    val progressColor = when {
        progress < 0.7f -> Color(0xFF4CAF50) // Green
        progress < 0.9f -> Color(0xFFFFA000) // Orange
        else -> Color(0xFFF44336) // Red
    }

    val formatter = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Group Icon Emoji
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1565C0).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = group.icon, fontSize = 24.sp)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = group.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = group.description, color = Color.Gray, fontSize = 13.sp, maxLines = 1)
                }

                // Member count badge
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${group.getMembersList().size}", fontSize = 12.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Budget Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Progres Anggaran", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = "Rp ${formatter.format(currentUsage)} / ${group.toFormattedBudget}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("👪") }

    val icons = listOf("👪", "🏠", "✈️", "🏪", "💼", "🎓", "💊")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Grup Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Grup") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget Awal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Pilih Ikon Grup:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    icons.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (selectedIcon == emoji) Color(0xFF1565C0).copy(alpha = 0.2f) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (name.isNotBlank() && budget.isNotBlank()) {
                        onConfirm(name, desc, budget.toDoubleOrNull() ?: 0.0, selectedIcon) 
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) { Text("Buat Grup") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun JoinGroupDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gabung Grup", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 8) code = it.uppercase() },
                    label = { Text("Kode Undangan") },
                    placeholder = { Text("Masukkan 8 karakter kode") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (code.length == 8) onConfirm(code) },
                enabled = code.length == 8,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) { Text("Gabung") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
