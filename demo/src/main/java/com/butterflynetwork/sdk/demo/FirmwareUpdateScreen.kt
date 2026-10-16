// Copyright 2012-2026 (C) Butterfly Network, Inc.

// Demo app — UI strings are intentionally hardcoded rather than extracted to string resources.

package com.butterflynetwork.sdk.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.butterflynetwork.sdk.demo.ui.theme.ImagingSDKTheme
import com.butterflynetwork.sdk.imaging.model.FirmwareUpdateProgress

@Composable
internal fun FirmwareUpdateScreen(
    updating: Boolean,
    progress: FirmwareUpdateProgress?,
    onUpdateFirmware: () -> Unit,
    isBatteryTooLow: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        Spacer(modifier = Modifier.weight(2f))

        Text(text = "⚠️")

        Text(text = "Probe firmware is out-of-date")

        if (!updating) {
            PrimaryActionButton(
                enabled = !isBatteryTooLow,
                text = "Update firmware",
                onClick = onUpdateFirmware,
            )

            if (isBatteryTooLow) {
                Text(
                    text = "Low probe battery. Charge your probe to 10% in order to update firmware.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Text(text = "Updating firmware...")

            Text(
                text = "Please keep the app open and iQ plugged in.",
                fontWeight = FontWeight.Bold,
            )

            if (progress != null) {
                Text(text = "${progress.timeRemaining.toInt()} seconds remaining")

                Text(text = "${(progress.fractionCompleted * 100).toInt()}%")

                LinearProgressIndicator(
                    progress = { progress.fractionCompleted },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.weight(3f))
    }
}

@Preview
@Composable
private fun PreviewBeforeUpdate() {
    ImagingSDKTheme {
        Surface {
            FirmwareUpdateScreen(
                updating = false,
                progress = null,
                onUpdateFirmware = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewBeforeUpdateWithLowBattery() {
    ImagingSDKTheme {
        Surface {
            FirmwareUpdateScreen(
                updating = false,
                progress = null,
                onUpdateFirmware = {},
                isBatteryTooLow = true,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewUpdatingNoProgress() {
    ImagingSDKTheme {
        Surface {
            FirmwareUpdateScreen(
                updating = true,
                progress = null,
                onUpdateFirmware = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewUpdating() {
    ImagingSDKTheme {
        Surface {
            FirmwareUpdateScreen(
                updating = true,
                progress = FirmwareUpdateProgress(fractionCompleted = 0.6f, timeRemaining = 24f),
                onUpdateFirmware = {},
            )
        }
    }
}
