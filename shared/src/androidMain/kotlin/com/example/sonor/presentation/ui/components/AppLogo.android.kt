package com.example.sonor.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
actual fun AppLogo(modifier: Modifier) {
    val context = LocalContext.current
    val resId = remember(context) {
        context.resources.getIdentifier("app_banner", "drawable", context.packageName)
    }
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "App Logo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
