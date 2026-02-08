package com.samidevstudio.pocketdex.ui.options

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samidevstudio.pocketdex.ui.theme.retroBackground
import com.samidevstudio.pocketdex.ui.theme.retroBorder

/**
 * OptionsScreen provides app-wide settings and user profile management.
 * Styled with a retro GameBoy menu aesthetic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    optionsViewModel: OptionsViewModel
) {
    // Dynamic checkered colors based on current theme surface
    val color1 = MaterialTheme.colorScheme.surface
    val isLight = color1.luminance() > 0.5f
    
    // Create a secondary color by compositing a tiny bit of contrast over the base
    val color2 = if (isLight) {
        Color.Black.copy(alpha = 0.05f).compositeOver(color1)
    } else {
        Color.White.copy(alpha = 0.05f).compositeOver(color1)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "OPTIONS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .retroBackground(color1 = color1, color2 = color2)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section: Profile
            item { MenuHeader("TRAINER INFO") }
            item {
                MenuItem(
                    label = "Trainer Name",
                    value = "Ash Ketchum",
                    icon = Icons.Default.Person
                )
            }

            // Section: Visuals
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { MenuHeader("VISUALS") }
            item {
                ToggleMenuItem(
                    label = "Dark Mode",
                    checked = optionsViewModel.isDarkTheme,
                    onCheckedChange = { optionsViewModel.toggleTheme() },
                    icon = Icons.Default.Brightness4
                )
            }

            // Section: Data
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { MenuHeader("MANAGEMENT") }
            item {
                MenuItem(
                    label = "Clear Cache",
                    icon = Icons.Default.DeleteSweep,
                    onClick = { /* TODO */ }
                )
            }
            item {
                MenuItem(
                    label = "Logout",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = { /* TODO */ },
                    textColor = Color.Red
                )
            }
        }
    }
}

@Composable
fun MenuHeader(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun MenuItem(
    label: String,
    value: String? = null,
    icon: ImageVector,
    textColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (textColor == Color.Red) Color.Red else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label.uppercase(),
                    modifier = Modifier.padding(start = 12.dp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (textColor == Color.Red) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
            if (value != null) {
                Text(
                    text = value,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ToggleMenuItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label.uppercase(),
                    modifier = Modifier.padding(start = 12.dp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFE3350D),
                    checkedTrackColor = Color(0xFFE3350D).copy(alpha = 0.5f)
                )
            )
        }
    }
}
