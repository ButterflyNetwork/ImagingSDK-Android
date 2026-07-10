// Copyright 2012-2026 (C) Butterfly Network, Inc.

// Demo app — UI strings are intentionally hardcoded rather than extracted to string resources.

package com.butterflynetwork.sdk.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImagingScreen(viewModel: ImagingViewModel) {
    val imagingState = viewModel.imagingState

    Box(modifier = Modifier.fillMaxSize()) {
        when (viewModel.stage) {
            ImagingViewModel.Stage.STARTING_UP -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Initializing...",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            ImagingViewModel.Stage.FIRMWARE_UPDATE_NEEDED -> {
                FirmwareUpdateScreen(
                    updating = viewModel.firmwareUpdating,
                    progress = viewModel.firmwareUpdateProgress,
                    onUpdateFirmware = { viewModel.updateFirmware() },
                )
            }

            ImagingViewModel.Stage.READY -> {
                val state = imagingState ?: return@Box
                ReadyScreen(
                    imagingState = state,
                    selectedPreset = viewModel.selectedPreset,
                    onPresetChange = { viewModel.setPreset(it) },
                    onStartImaging = { viewModel.startImaging() },
                    onConnectSimulatedProbe = { viewModel.connectSimulatedProbe() },
                    onDisconnectSimulatedProbe = { viewModel.disconnectSimulatedProbe() },
                )
            }

            ImagingViewModel.Stage.STARTING_IMAGING -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            }

            ImagingViewModel.Stage.IMAGING -> {
                val state = imagingState ?: return@Box
                LiveImagingScreen(
                    imagingState = state,
                    selectedPreset = viewModel.selectedPreset,
                    selectedDepth = viewModel.selectedDepth,
                    selectedMode = viewModel.selectedMode,
                    selectedMModeAngle = viewModel.selectedMModeAngle,
                    selectedPlanePosition = viewModel.selectedPlanePosition,
                    onPresetChange = { viewModel.setPreset(it) },
                    onDepthChange = { depth, interactive ->
                        viewModel.setDepth(depth, interactive)
                    },
                    onGainChange = { viewModel.setGain(it) },
                    onColorGainChange = { viewModel.setColorGain(it) },
                    onModeChange = { viewModel.setMode(it) },
                    onMModeAngleChange = { angle, finished ->
                        viewModel.setMModeAngle(angle, finished)
                    },
                    onPlanePositionChange = { position, finished ->
                        viewModel.setPlanePosition(position, finished)
                    },
                    onStopImaging = { viewModel.stopImaging() },
                )
            }
        }

        // Info overlay (bottom-left)
        InfoOverlay(
            probe = imagingState?.probe,
            licenseState = viewModel.licenseState,
        )
    }

    if (viewModel.showSweepFrames) {
        SweepFramesModal(
            frames = viewModel.sweepFrames,
            onDismiss = { viewModel.onSweepFramesModalDismiss() },
        )
    }

    // Error dialog (e.g. license key validation failure on startup).
    viewModel.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            },
        )
    }
}
