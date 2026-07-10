// Copyright 2012-2026 (C) Butterfly Network, Inc.

package com.butterflynetwork.sdk.demo

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.butterflynetwork.sdk.imaging.ButterflyImaging
import com.butterflynetwork.sdk.imaging.model.FirmwareUpdateProgress
import com.butterflynetwork.sdk.imaging.model.ImagingLogListener
import com.butterflynetwork.sdk.imaging.model.ImagingPreset
import com.butterflynetwork.sdk.imaging.model.ImagingState
import com.butterflynetwork.sdk.imaging.model.ImagingStateChanges
import com.butterflynetwork.sdk.imaging.model.ImagingStateListener
import com.butterflynetwork.sdk.imaging.model.LicenseState
import com.butterflynetwork.sdk.imaging.model.LicenseStatesListener
import com.butterflynetwork.sdk.imaging.model.LogLevel
import com.butterflynetwork.sdk.imaging.model.PlaneLocation
import com.butterflynetwork.sdk.imaging.model.PresetParameters
import com.butterflynetwork.sdk.imaging.model.ProbeState
import com.butterflynetwork.sdk.imaging.model.UltrasoundMode
import com.butterflynetwork.sdk.imaging.model.ValidationError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors

class ImagingViewModel(application: Application) : AndroidViewModel(application) {

    // A dedicated single thread for all imaging operations is recommended.
    private val imagingExecutor by lazy {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "ImagingThread")
        }
    }

    // Dedicated dispatcher that uses the imaging thread.
    private val imagingDispatcher: CoroutineDispatcher by lazy {
        imagingExecutor.asCoroutineDispatcher()
    }

    enum class Stage {
        STARTING_UP,
        READY,
        FIRMWARE_UPDATE_NEEDED,
        STARTING_IMAGING,
        IMAGING,
    }

    // Published state.

    var stage by mutableStateOf(Stage.STARTING_UP)
        private set

    var imagingState by mutableStateOf<ImagingState?>(null)
        private set

    var firmwareUpdating by mutableStateOf(false)
        private set

    var firmwareUpdateProgress by mutableStateOf<FirmwareUpdateProgress?>(null)
        private set

    var sweepFrames by mutableStateOf<List<Bitmap>>(emptyList())
        private set

    var showSweepFrames by mutableStateOf(false)
        private set

    // Error to surface to the user (e.g. license key validation failure on startup).
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var licenseState by mutableStateOf<LicenseState>(LicenseState.Invalid)
        private set

    // User-selected control state.

    var selectedPreset by mutableStateOf<ImagingPreset?>(null)
        private set

    var selectedDepth by mutableFloatStateOf(0f)
        private set

    var selectedMode by mutableStateOf(UltrasoundMode.B_MODE)
        private set

    var selectedMModeAngle by mutableFloatStateOf(0f)
        private set

    var selectedPlanePosition by mutableFloatStateOf(0f)
        private set

    private val imaging: ButterflyImaging by lazy {
        ButterflyImaging.getSharedInstance(application).apply {
            // Set up imaging state callback.
            imagingStateListener = object : ImagingStateListener {
                override fun onImagingStateChange(
                    imagingState: ImagingState,
                    imagingStateChanges: ImagingStateChanges,
                ) {
                    updateState(imagingState, imagingStateChanges)
                    Timber.d("bModeImageRect: ${imagingState.bModeImageRect}")
                    Timber.d("bModeVirtualApex: ${imagingState.bModeVirtualApex}")
                }
            }

            // Set up imaging log callback if needed.
            imagingLogListener = object : ImagingLogListener {
                override fun onLog(
                    loggingLevel: LogLevel,
                    tag: String,
                    message: String,
                ) {
                    Timber.tag(tag).d(message)
                }
            }

            // Set up license state callback.
            licenseStatesListener = object : LicenseStatesListener {
                override fun onLicenseStateChange(state: LicenseState) {
                    licenseState = state
                }
            }
        }
    }

    private fun updateState(state: ImagingState, stateChanges: ImagingStateChanges) {
        // Reset when probe disconnected.
        if (state.probe.state == ProbeState.DISCONNECTED) {
            resetControlsForPreset(null)
        }

        // When the active preset changed, reset selected controls.
        if (imagingState?.preset != state.preset) {
            state.preset?.let { resetControlsForPreset(it) }
        }

        imagingState = state

        // Firmware update needed — transition from any stage.
        if (state.probe.state == ProbeState.FIRMWARE_INCOMPATIBLE &&
            stage != Stage.FIRMWARE_UPDATE_NEEDED
        ) {
            stage = Stage.FIRMWARE_UPDATE_NEEDED
            return
        }

        // Accumulate sweep frames in MultiSlice mode.
        if (state.mode == UltrasoundMode.MULTI_SLICE && stateChanges.bModeImageChanged) {
            state.bModeImage?.let { bModeImage ->
                if (!showSweepFrames) {
                    // Reset on start.
                    if (bModeImage.metadata?.sweepFrameIndex == 0) {
                        sweepFrames = emptyList()
                    }

                    // Accumulate sweep frames.
                    sweepFrames = sweepFrames + bModeImage.image

                    // Finish sweeping and show modal when get the last frame.
                    if (bModeImage.metadata?.isLastSweepFrame == true) {
                        showSweepFrames = true

                        // Also stop imaging.
                        stopImaging()
                    }
                }
            }
        }

        // Update stage based on state transitions.
        when (stage) {
            Stage.STARTING_UP -> {
                stage = Stage.READY
            }

            Stage.READY -> {
                // Stay ready
            }

            Stage.FIRMWARE_UPDATE_NEEDED -> {
                if (state.probe.state != ProbeState.FIRMWARE_INCOMPATIBLE) {
                    stage = Stage.READY
                }
            }

            Stage.STARTING_IMAGING -> {
                // If we have a new image in, we are imaging.
                if (stateChanges.bModeImageChanged && imagingState?.bModeImage != null) {
                    stage = Stage.IMAGING
                }
                // If probe disconnects while starting, revert.
                if (state.probe.state == ProbeState.DISCONNECTED) {
                    stopImaging()
                }
            }

            Stage.IMAGING -> {
                if (state.probe.state == ProbeState.DISCONNECTED) {
                    stopImaging()
                }
            }
        }
    }

    private fun resetControlsForPreset(preset: ImagingPreset?) {
        selectedPreset = preset
        selectedDepth = preset?.defaultDepth ?: 0f
        selectedMode = UltrasoundMode.B_MODE
        selectedMModeAngle = 0f
        selectedPlanePosition = 0f
    }

    fun startup() {
        runOnImagingDispatcher {
            // Skip if already started.
            if (stage != Stage.STARTING_UP) return@runOnImagingDispatcher

            try {
                imaging.startup(clientKey = BuildConfig.LICENSE_KEY)
            } catch (e: ValidationError) {
                // License key validation failed (bad key, expired, or network error).
                errorMessage = e.message ?: "License validation failed."
                Timber.e(e, "Failed to start up backend")
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun startImaging() {
        val preset = selectedPreset
        val parameters = preset?.let {
            PresetParameters.from(it).copy(depth = selectedDepth)
        }

        stage = Stage.STARTING_IMAGING
        runOnImagingDispatcher {
            imaging.startImaging(preset, parameters)
        }
    }

    fun stopImaging() {
        stage = Stage.READY
        runOnImagingDispatcher {
            imaging.stopImaging()
        }
    }

    fun connectSimulatedProbe() {
        runOnImagingDispatcher {
            imaging.connectSimulatedProbe()
        }
    }

    fun disconnectSimulatedProbe() {
        // Note: it takes some time for the probe state to become disconnected after disabling
        // onesInjector.
        runOnImagingDispatcher {
            imaging.disconnectSimulatedProbe()
        }
    }

    fun setPreset(preset: ImagingPreset) {
        resetControlsForPreset(preset)
        runOnImagingDispatcher {
            imaging.setPreset(preset, null)
            imaging.setMode(UltrasoundMode.B_MODE) // Reset to BMode when preset changed.
        }
    }

    fun setDepth(depth: Float, interactive: Boolean = false) {
        selectedDepth = depth
        runOnImagingDispatcher {
            imaging.setDepth(depth, interactive)
        }
    }

    fun setGain(gain: Int) {
        runOnImagingDispatcher {
            imaging.setGain(gain)
        }
    }

    fun setColorGain(colorGain: Int) {
        runOnImagingDispatcher {
            imaging.setColorGain(colorGain)
        }
    }

    fun setMode(mode: UltrasoundMode) {
        selectedMode = mode
        runOnImagingDispatcher {
            imaging.setMode(mode)
        }
    }

    fun setMModeAngle(angleDegrees: Float, finished: Boolean = false) {
        selectedMModeAngle = angleDegrees
        if (finished) {
            runOnImagingDispatcher {
                imaging.setMModeAngle(angleDegrees)
            }
        }
    }

    fun setPlanePosition(position: Float, finished: Boolean = false) {
        selectedPlanePosition = position

        if (finished) {
            val currPreset = imagingState?.preset ?: return
            val planeLocation =
                if (currPreset.isLinearPreset) {
                    PlaneLocation.Linear(position)
                } else {
                    PlaneLocation.Sector(position)
                }

            runOnImagingDispatcher { imaging.setPlaneLocation(planeLocation) }
        }
    }

    fun onSweepFramesModalDismiss() {
        showSweepFrames = false
        sweepFrames = emptyList()

        // Resume imaging in BMode
        setMode(UltrasoundMode.B_MODE)
        startImaging()
    }

    fun updateFirmware() {
        if (firmwareUpdating) return
        viewModelScope.launch(imagingDispatcher) {
            firmwareUpdating = true
            imaging.updateFirmware()
                .catch { e ->
                    Timber.e(e, "Firmware update failed")
                }
                .collect { progress ->
                    firmwareUpdateProgress = progress
                }
            firmwareUpdating = false
            firmwareUpdateProgress = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Shutdown the imaging thread executor
        imagingExecutor.shutdown()
        Timber.d("ImagingViewModel cleared, imaging thread shutdown")
    }

    /**
     * Run all imaging functions in same thread is recommended.
     */
    private fun runOnImagingDispatcher(function: suspend () -> Unit) {
        viewModelScope.launch(imagingDispatcher) {
            function()
        }
    }
}
