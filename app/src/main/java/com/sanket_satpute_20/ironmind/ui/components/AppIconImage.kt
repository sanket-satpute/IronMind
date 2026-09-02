package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

@Composable
fun AppIconImage(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawable = remember(packageName) { runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull() }
    if (drawable != null) {
        Image(bitmap = drawable.toBitmap(64, 64).asImageBitmap(), contentDescription = null, modifier = modifier)
    } else {
        Box(modifier = modifier.background(Color.DarkGray, CircleShape))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun AppIconImagePreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        AppIconImage(packageName = "com.example")
    }
}
