// Copyright 2012-2026 (C) Butterfly Network, Inc.

// Demo app — UI strings are intentionally hardcoded rather than extracted to string resources.

package com.butterflynetwork.sdk.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.butterflynetwork.sdk.demo.ui.theme.ImagingSDKTheme
import com.butterflynetwork.sdk.imaging.model.ImagingPreset
import com.butterflynetwork.sdk.imaging.model.ImagingState
import com.butterflynetwork.sdk.imaging.model.ProbeState

@Composable
internal fun ReadyScreen(
    imagingState: ImagingState,
    selectedPreset: ImagingPreset?,
    onStartImaging: () -> Unit,
    onPresetChange: (ImagingPreset) -> Unit,
    onConnectSimulatedProbe: () -> Unit,
    onDisconnectSimulatedProbe: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            imagingState.probe.state == ProbeState.HARDWARE_INCOMPATIBLE -> {
                Text("Probe incompatible.")
            }

            imagingState.probe.state == ProbeState.DISCONNECTED -> {
                PrimaryActionButton(
                    text = "Connect a simulated probe",
                    onClick = onConnectSimulatedProbe,
                )
            }

            imagingState.availablePresets.isNotEmpty() -> {
                if (imagingState.probe.isSimulated) {
                    PrimaryActionButton(
                        text = "Disconnect simulated probe",
                        onClick = onDisconnectSimulatedProbe,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetPicker(
                        modifier = Modifier.weight(1f),
                        selectedPreset = selectedPreset,
                        availablePresets = imagingState.availablePresets,
                        onPresetChange = onPresetChange,
                    )

                    PrimaryActionButton(
                        text = "Start imaging",
                        onClick = onStartImaging,
                    )
                }
            }

            else -> {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun PreviewDisconnected() {
    ImagingSDKTheme {
        Surface {
            ReadyScreen(
                imagingState = previewImagingState(
                    probe = previewProbe(state = ProbeState.DISCONNECTED),
                ),
                selectedPreset = null,
                onStartImaging = {},
                onPresetChange = {},
                onConnectSimulatedProbe = {},
                onDisconnectSimulatedProbe = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewReady() {
    ImagingSDKTheme {
        Surface {
            val preset = previewImagingPreset()
            ReadyScreen(
                imagingState = previewImagingState(probe = previewProbe(state = ProbeState.READY)),
                selectedPreset = preset,
                onStartImaging = {},
                onPresetChange = {},
                onConnectSimulatedProbe = {},
                onDisconnectSimulatedProbe = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewHardwareIncompatible() {
    ImagingSDKTheme {
        Surface {
            ReadyScreen(
                imagingState = previewImagingState(
                    probe = previewProbe(state = ProbeState.HARDWARE_INCOMPATIBLE),
                    availablePresets = emptyList(),
                ),
                selectedPreset = null,
                onStartImaging = {},
                onPresetChange = {},
                onConnectSimulatedProbe = {},
                onDisconnectSimulatedProbe = {},
            )
        }
    }
}
