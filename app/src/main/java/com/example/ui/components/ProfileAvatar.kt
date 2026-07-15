package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.ui.theme.NutriBlue
import com.example.ui.theme.NutriBlueLight

@Composable
fun ProfileAvatar(
    avatarUri: String?,
    name: String,
    size: Dp = 64.dp,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .let {
            if (onClick != null) {
                it.clickable { onClick() }
            } else {
                it
            }
        }
        .testTag("profile_avatar_image")

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (avatarUri == null) {
            val firstChar = name.trim().firstOrNull()?.uppercase() ?: "N"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NutriBlue, NutriBlueLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstChar,
                    color = Color.White,
                    fontSize = (size.value * 0.45f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (avatarUri.startsWith("preset_")) {
            val (emoji, bg) = when (avatarUri) {
                "preset_veggie" -> "🥦" to Color(0xFFE8F5E9)
                "preset_apple" -> "🍎" to Color(0xFFFFEBEE)
                "preset_gym" -> "🏋️" to Color(0xFFECEFF1)
                "preset_water" -> "💧" to Color(0xFFE3F2FD)
                "preset_runner" -> "🏃" to Color(0xFFFFF8E1)
                "preset_zen" -> "🧘" to Color(0xFFF3E5F5)
                "preset_avocado" -> "🥑" to Color(0xFFF1F8E9)
                "preset_salad" -> "🥗" to Color(0xFFE0F2F1)
                "preset_egg" -> "🍳" to Color(0xFFFFF3E0)
                "preset_fish" -> "🐟" to Color(0xFFE0F7FA)
                else -> "👤" to Color(0xFFF5F5F5)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = (size.value * 0.45f).sp)
            }
        } else {
            val imageModel = if (avatarUri.startsWith("/") || avatarUri.startsWith("file:")) {
                java.io.File(avatarUri)
            } else {
                avatarUri
            }
            val painter = rememberAsyncImagePainter(model = imageModel)
            if (painter.state is AsyncImagePainter.State.Error) {
                Image(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Foto Profil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            } else {
                Image(
                    painter = painter,
                    contentDescription = "Foto Profil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
