// Copyright 2012-2026 (C) Butterfly Network, Inc.

package com.butterflynetwork.sdk.demo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun ImagingSDKTheme(content: @Composable () -> Unit) =
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Blue80,
            onPrimary = PurpleGrey80,
            surface = Black90,
            onSurface = PurpleGrey80,
        ),
        content = content,
    )
