// Copyright 2012-2026 (C) Butterfly Network, Inc.

package com.butterflynetwork.sdk.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.butterflynetwork.sdk.demo.ui.theme.ImagingSDKTheme
import com.butterflynetwork.sdk.imaging.model.ImagingPreset
import com.butterflynetwork.sdk.imaging.model.ImagingState
import com.butterflynetwork.sdk.imaging.model.Rect
import com.butterflynetwork.sdk.imaging.model.UltrasoundMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun LiveImagingScreen(
    imagingState: ImagingState,
    selectedPreset: ImagingPreset?,
    selectedDepth: Float,
    selectedMode: UltrasoundMode,
    selectedMModeAngle: Float,
    selectedPlanePosition: Float,
    onPresetChange: (ImagingPreset) -> Unit,
    onDepthChange: (Float, Boolean) -> Unit,
    onGainChange: (Int) -> Unit,
    onColorGainChange: (Int) -> Unit,
    onModeChange: (UltrasoundMode) -> Unit,
    onMModeAngleChange: (Float, Boolean) -> Unit,
    onPlanePositionChange: (Float, Boolean) -> Unit,
    onStopImaging: () -> Unit,
) {
    ImagesContent(
        imagingState = imagingState,
        mModeAngle = selectedMModeAngle,
        planePosition = selectedPlanePosition,
        isLinearPreset = selectedPreset?.isLinearPreset ?: false,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PresetPicker(
                modifier = Modifier.weight(1f),
                selectedPreset = selectedPreset,
                availablePresets = imagingState.availablePresets,
                onPresetChange = onPresetChange,
            )

            PrimaryActionButton(
                text = "Stop imaging",
                onClick = onStopImaging,
            )
        }

        val supportedModes = imagingState.preset?.supportedModes.orEmpty()
        if (supportedModes.size > 1) {
            ModePicker(
                selectedMode = selectedMode,
                availableModes = supportedModes,
                onModeChange = onModeChange,
            )
        }

        // Disable controls for multi-slice mode.
        val enableControls = selectedMode != UltrasoundMode.MULTI_SLICE

        // Depth slider.
        selectedPreset?.let { preset ->
            BoundedSlider(
                enabled = enableControls,
                startLabel = "Depth: ${(preset.depthBounds.lower * 100).toInt()}cm",
                value = selectedDepth,
                valueRange = preset.depthBounds.lower..preset.depthBounds.upper,
                endLabel = "${(preset.depthBounds.upper * 100).toInt()}cm",
                onValueChange = { onDepthChange(it, true) },
                onValueChangeFinished = { onDepthChange(selectedDepth, false) },
            )
        }

        // Gain slider.
        BoundedSlider(
            enabled = enableControls,
            startLabel = "Gain: 0",
            value = imagingState.gain.toFloat(),
            valueRange = 0f..100f,
            endLabel = "100",
            onValueChange = { onGainChange(it.toInt()) },
        )

        // Color gain slider / MMode angle slider / Plane position slider.
        when (selectedMode) {
            UltrasoundMode.COLOR_DOPPLER -> {
                BoundedSlider(
                    startLabel = "Color Gain: 0",
                    value = imagingState.colorGain.toFloat(),
                    valueRange = 0f..100f,
                    endLabel = "100",
                    onValueChange = { onColorGainChange(it.toInt()) },
                )
            }

            UltrasoundMode.M_MODE -> {
                val maxAngle = imagingState.mModeAngleMax.toFloat()
                BoundedSlider(
                    startLabel = "M-Mode Angle:",
                    value = selectedMModeAngle,
                    valueRange = -maxAngle..maxAngle,
                    onValueChange = { onMModeAngleChange(it, false) },
                    onValueChangeFinished = { onMModeAngleChange(selectedMModeAngle, true) },
                )
            }

            UltrasoundMode.BIPLANE -> {
                imagingState.biplaneParameters?.let { params ->
                    selectedPreset?.let { preset ->
                        val startLabel =
                            if (preset.isLinearPreset) "Plane Position:" else "Plane Angle:"
                        val maxValue =
                            if (preset.isLinearPreset) params.maxXMeters else params.maxAngleDegrees
                        BoundedSlider(
                            startLabel = startLabel,
                            value = selectedPlanePosition,
                            valueRange = -maxValue..maxValue,
                            onValueChange = { onPlanePositionChange(it, false) },
                            onValueChangeFinished = {
                                onPlanePositionChange(selectedPlanePosition, true)
                            },
                        )
                    }
                }
            }

            UltrasoundMode.MULTI_SLICE -> {
                imagingState.bModeImage?.metadata?.let { meta ->
                    val index = meta.sweepFrameIndex
                    val total = meta.sweepTotalFrames
                    if (index != null && total != null) {
                        Text(
                            text = "Frame ${index + 1} of $total",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun ImagesContent(
    imagingState: ImagingState,
    mModeAngle: Float,
    planePosition: Float,
    isLinearPreset: Boolean,
) =
    // Display the image(s) for the active mode.
    when (imagingState.mode) {
        UltrasoundMode.M_MODE -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    imagingState.bModeImage?.image?.let {
                        Box {
                            // One more box so that Overlay has same size/position as Image.
                            Image(
                                modifier = Modifier.aspectRatio(it.width / it.height.toFloat()),
                                bitmap = it.asImageBitmap(),
                                contentScale = ContentScale.Fit,
                                contentDescription = "B-Mode Image",
                            )
                            MModeLineOverlay(
                                modifier = Modifier.matchParentSize(),
                                angleDegrees = mModeAngle,
                            )
                        }
                    }
                }

                imagingState.mModeImage?.image?.let {
                    Image(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        bitmap = it.asImageBitmap(),
                        contentScale = ContentScale.Fit,
                        contentDescription = "M-Mode Image",
                    )
                } ?: Spacer(modifier = Modifier.weight(1f))
            }
        }

        UltrasoundMode.BIPLANE -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    imagingState.bModeImage?.let { bModeImage ->
                        Box {
                            // One more box so that Overlay has same size/position as Image.
                            Image(
                                modifier = Modifier.aspectRatio(
                                    bModeImage.image.width / bModeImage.image.height.toFloat(),
                                ),
                                bitmap = bModeImage.image.asImageBitmap(),
                                contentScale = ContentScale.Fit,
                                contentDescription = "B-Mode Image",
                            )
                            imagingState.biplaneParameters?.let {
                                PerpPlaneLineOverlay(
                                    modifier = Modifier.matchParentSize(),
                                    isLinearPreset = isLinearPreset,
                                    imageRectInMeters = bModeImage.realWorldCoordinates,
                                    planePosition = planePosition,
                                )
                            }
                        }
                    }
                }

                imagingState.biplaneSecondaryImage?.image?.let {
                    Image(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        bitmap = it.asImageBitmap(),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Biplane Image",
                    )
                } ?: Spacer(modifier = Modifier.weight(1f))
            }
        }

        else -> {
            imagingState.bModeImage?.image?.let { bitmap ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "B-Mode Image",
                )
            }
        }
    }

private val lineToolColor = Color.Cyan.copy(alpha = 0.8f)
private val lineToolStrokeWidth = 2.dp

@Composable
private fun MModeLineOverlay(
    modifier: Modifier = Modifier,
    angleDegrees: Float,
) {
    Canvas(modifier = modifier) {
        clipRect {
            val centerX = size.width / 2f
            val length = maxOf(size.width, size.height)
            val angleRad = angleDegrees * PI / 180.0
            drawLine(
                color = lineToolColor,
                start = Offset(centerX, 0f),
                end = Offset(
                    (centerX + length * sin(angleRad)).toFloat(),
                    (length * cos(angleRad)).toFloat(),
                ),
                strokeWidth = lineToolStrokeWidth.toPx(),
            )
        }
    }
}

@Composable
private fun PerpPlaneLineOverlay(
    modifier: Modifier = Modifier,
    isLinearPreset: Boolean,
    imageRectInMeters: Rect,
    planePosition: Float,
) {
    Canvas(modifier = modifier) {
        clipRect {
            if (isLinearPreset) {
                val pixelsPerMeter = size.width / imageRectInMeters.width
                val positionX = ((planePosition - imageRectInMeters.x) * pixelsPerMeter).toFloat()
                drawLine(
                    color = lineToolColor,
                    start = Offset(positionX, 0f),
                    end = Offset(positionX, size.height),
                    strokeWidth = lineToolStrokeWidth.toPx(),
                )
            } else {
                val angleRad = planePosition * PI / 180.0
                val centerX = size.width / 2f
                val length = maxOf(size.width, size.height)
                drawLine(
                    color = lineToolColor,
                    start = Offset(centerX, 0f),
                    end = Offset(
                        (centerX + length * sin(angleRad)).toFloat(),
                        (length * cos(angleRad)).toFloat(),
                    ),
                    strokeWidth = lineToolStrokeWidth.toPx(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLiveImaging() {
    ImagingSDKTheme {
        Surface {
            val preset = previewImagingPreset()
            LiveImagingScreen(
                imagingState = previewImagingState(),
                selectedPreset = preset,
                selectedDepth = preset.defaultDepth,
                selectedMode = UltrasoundMode.B_MODE,
                selectedMModeAngle = 0f,
                selectedPlanePosition = 0f,
                onPresetChange = {},
                onDepthChange = { _, _ -> },
                onGainChange = {},
                onColorGainChange = {},
                onModeChange = {},
                onMModeAngleChange = { _, _ -> },
                onPlanePositionChange = { _, _ -> },
                onStopImaging = {},
            )
        }
    }
}
