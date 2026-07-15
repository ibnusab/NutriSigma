package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.example.data.model.ReminderEntity
import com.example.ui.components.ProfileAvatar
import com.example.ui.theme.NutriBlue
import com.example.ui.theme.NutriBlueLight
import com.example.ui.theme.NutriGreen
import com.example.ui.theme.NutriOrange
import com.example.ui.theme.NutriRed
import com.example.viewmodel.NutriViewModel

@Composable
fun ProfileScreen(
    viewModel: NutriViewModel,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val reminders by viewModel.remindersList.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var targetWeightStr by remember { mutableStateOf("") }
    var isDarkMode by remember { mutableStateOf(false) }

    var isEditingProfile by remember { mutableStateOf(false) }

    var showAvatarDialog by remember { mutableStateOf(false) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val localPath = withContext(Dispatchers.IO) {
                        try {
                            val resolver = context.applicationContext.contentResolver
                            resolver.openInputStream(uri)?.use { inputStream ->
                                val fileName = "profile_avatar_${System.currentTimeMillis()}.jpg"
                                val outputFile = File(context.applicationContext.filesDir, fileName)
                                
                                // Delete old avatars
                                try {
                                    context.applicationContext.filesDir.listFiles()?.forEach { file ->
                                        if (file.name.startsWith("profile_avatar_") && file.name.endsWith(".jpg")) {
                                            file.delete()
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                
                                FileOutputStream(outputFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                                outputFile.absolutePath
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    if (localPath != null) {
                        viewModel.updateAvatar(localPath)
                    }
                    showAvatarDialog = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Synchronize inputs with user state
    LaunchedEffect(user) {
        if (user != null && !isEditingProfile) {
            name = user!!.name
            targetWeightStr = user!!.targetWeight.toString()
            isDarkMode = user!!.isDarkMode
        }
    }

    // AVATAR PICKER DIALOG
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = {
                Text(
                    text = "Pilih Foto Profil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pilih dari karakter sehat favorit Anda atau ambil foto dari galeri HP.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Grid of Presets
                    val presets = listOf(
                        "preset_veggie" to "🥦",
                        "preset_apple" to "🍎",
                        "preset_gym" to "🏋️",
                        "preset_water" to "💧",
                        "preset_runner" to "🏃",
                        "preset_zen" to "🧘",
                        "preset_avocado" to "🥑",
                        "preset_salad" to "🥗",
                        "preset_egg" to "🍳",
                        "preset_fish" to "🐟"
                    )

                    Column {
                        val chunked = presets.chunked(5)
                        chunked.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowItems.forEach { (id, emoji) ->
                                    val isSelected = user?.avatarUri == id
                                    val bg = when (id) {
                                        "preset_veggie" -> Color(0xFFE8F5E9)
                                        "preset_apple" -> Color(0xFFFFEBEE)
                                        "preset_gym" -> Color(0xFFECEFF1)
                                        "preset_water" -> Color(0xFFE3F2FD)
                                        "preset_runner" -> Color(0xFFFFF8E1)
                                        "preset_zen" -> Color(0xFFF3E5F5)
                                        "preset_avocado" -> Color(0xFFF1F8E9)
                                        "preset_salad" -> Color(0xFFE0F2F1)
                                        "preset_egg" -> Color(0xFFFFF3E0)
                                        "preset_fish" -> Color(0xFFE0F7FA)
                                        else -> Color(0xFFF5F5F5)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(bg)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) NutriBlue else MaterialTheme.colorScheme.outlineVariant,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.updateAvatar(id)
                                                showAvatarDialog = false
                                            }
                                            .testTag("preset_$id"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 22.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Gallery option button
                    Button(
                        onClick = {
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NutriBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("select_from_gallery_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ambil dari Galeri HP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (user?.avatarUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                viewModel.updateAvatar(null)
                                showAvatarDialog = false
                            },
                            modifier = Modifier.testTag("remove_avatar_btn")
                        ) {
                            Text("Hapus Foto", color = NutriRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        // PROFILE HEADER CARD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive Profile Avatar with camera edit badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clickable { showAvatarDialog = true }
                    .testTag("avatar_badge_container"),
                contentAlignment = Alignment.BottomEnd
            ) {
                ProfileAvatar(
                    avatarUri = user?.avatarUri,
                    name = user?.name ?: "Pengguna Nutri",
                    size = 72.dp,
                    onClick = { showAvatarDialog = true }
                )

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(NutriBlue)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Foto",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = user?.name ?: "Pengguna Nutri",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = user?.email ?: "email@nutrisigma.com",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // EDIT PROFILE PROFILE CARD
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengaturan Profil & Target",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = {
                            if (isEditingProfile) {
                                // Save
                                val weight = targetWeightStr.toDoubleOrNull() ?: user?.targetWeight ?: 65.0
                                viewModel.updateProfileSettings(name, weight, isDarkMode)
                                isEditingProfile = false
                            } else {
                                isEditingProfile = true
                            }
                        }
                    ) {
                        Text(
                            text = if (isEditingProfile) "Simpan" else "Ubah",
                            fontWeight = FontWeight.Bold,
                            color = NutriBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isEditingProfile) {
                    // Editable Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Editable Target Weight
                    OutlinedTextField(
                        value = targetWeightStr,
                        onValueChange = { targetWeightStr = it },
                        label = { Text("Target Berat Badan (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Read only view
                    ProfileItemRow(icon = Icons.Default.Person, label = "Nama", value = name)
                    ProfileItemRow(icon = Icons.Default.TrackChanges, label = "Target Berat Badan", value = "$targetWeightStr kg")
                    ProfileItemRow(icon = Icons.Default.Info, label = "Target Gizi Saat Ini", value = user?.goalType ?: "Menjaga Berat Badan")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DYNAMIC ALARM REMINDERS LIST CARD
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = NutriOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pengingat Rutinitas Harian",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Konfigurasikan jadwal notifikasi untuk menjaga konsistensi diet Anda.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (reminders.isEmpty()) {
                    Text(
                        text = "Tidak ada pengingat aktif.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                } else {
                    reminders.forEach { reminder ->
                        ReminderRowItem(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminder(reminder) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // THEME / PREFERENCES CARD
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = NutriBlueLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mode Gelap (Dark Mode)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            isDarkMode = it
                            val weight = targetWeightStr.toDoubleOrNull() ?: user?.targetWeight ?: 65.0
                            viewModel.updateProfileSettings(name, weight, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NutriBlue)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // LOGOUT BUTTON
        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NutriRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOGOUT DARI AKUN", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun ProfileItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ReminderRowItem(
    reminder: ReminderEntity,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val emoji = when (reminder.type) {
                "Water" -> "💧"
                "Breakfast" -> "🍳"
                "Lunch" -> "🍛"
                "Dinner" -> "🐟"
                else -> "🏋️"
            }
            Text(emoji, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
            Column {
                Text(
                    text = reminder.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "Setiap hari • ${reminder.timeStr}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = reminder.isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedThumbColor = NutriBlue)
        )
    }
}


