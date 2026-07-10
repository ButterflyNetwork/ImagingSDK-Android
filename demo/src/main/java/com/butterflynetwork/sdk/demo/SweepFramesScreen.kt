// Copyright 2012-2026 (C) Butterfly Network, Inc.

// Demo app — UI strings are intentionally hardcoded rather than extracted to string resources.

package com.butterflynetwork.sdk.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.butterflynetwork.sdk.demo.ui.theme.ImagingSDKTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SweepFramesModal(
    frames: List<Bitmap>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        modifier = Modifier.fillMaxSize(),
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "Sweep Frames (${frames.size})",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .navigationBarsPadding(),
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(frames) { index, bitmap ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        modifier = Modifier.aspectRatio(bitmap.width / bitmap.height.toFloat()),
                        bitmap = bitmap.asImageBitmap(),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Frame ${index + 1}",
                    )
                    Text(
                        text = "Frame ${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewSweepFrames() {
    ImagingSDKTheme {
        Surface {
            val resources = LocalResources.current
            val frames = remember {
                val bitmap = BitmapFactory.decodeResource(
                    resources,
                    android.R.drawable.ic_media_play,
                )
                List(5) { bitmap }
            }
            SweepFramesModal(frames) { }
        }
    }
}
