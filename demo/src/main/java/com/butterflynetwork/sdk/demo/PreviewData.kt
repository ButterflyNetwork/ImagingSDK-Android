// Copyright 2012-2026 (C) Butterfly Network, Inc.

package com.butterflynetwork.sdk.demo

import android.graphics.PointF
import com.butterflynetwork.sdk.imaging.model.AcousticPower
import com.butterflynetwork.sdk.imaging.model.BatteryState
import com.butterflynetwork.sdk.imaging.model.Bounds
import com.butterflynetwork.sdk.imaging.model.ColorBoxConstraintParameters
import com.butterflynetwork.sdk.imaging.model.DefaultColorBoxParameters
import com.butterflynetwork.sdk.imaging.model.FlowVelocityScaleMode
import com.butterflynetwork.sdk.imaging.model.ImageOrientation
import com.butterflynetwork.sdk.imaging.model.ImagingPreset
import com.butterflynetwork.sdk.imaging.model.ImagingState
import com.butterflynetwork.sdk.imaging.model.LinearColorBox
import com.butterflynetwork.sdk.imaging.model.PolarColorBox
import com.butterflynetwork.sdk.imaging.model.PracticeType
import com.butterflynetwork.sdk.imaging.model.Probe
import com.butterflynetwork.sdk.imaging.model.ProbeState
import com.butterflynetwork.sdk.imaging.model.ProbeType
import com.butterflynetwork.sdk.imaging.model.Rect
import com.butterflynetwork.sdk.imaging.model.TemperatureState
import com.butterflynetwork.sdk.imaging.model.ThermalIndexValues
import com.butterflynetwork.sdk.imaging.model.TraceScrollSpeedMode
import com.butterflynetwork.sdk.imaging.model.UltrasoundMode

internal fun previewImagingPreset(name: String = "Abdomen") =
    ImagingPreset(
        uuid = "preview-$name",
        name = name,
        bannerName = name,
        supportedModes = listOf(
            UltrasoundMode.B_MODE,
            UltrasoundMode.COLOR_DOPPLER,
            UltrasoundMode.M_MODE,
        ),
        depthBounds = Bounds(lower = 0.02f, upper = 0.24f),
        defaultDepth = 0.12f,
        defaultGain = 50,
        defaultColorGain = 50,
        isLinearPreset = false,
        isVeterinary = false,
        defaultImageOrientation = ImageOrientation.LEFT_TO_RIGHT,
        defaultFlowVelocityScaleMode = FlowVelocityScaleMode.SLOW,
        defaultTgcNear = 0,
        defaultTgcMid = 0,
        defaultTgcFar = 0,
        defaultMModeAngle = 0.0,
    )

internal fun previewProbe(state: ProbeState = ProbeState.READY) =
    Probe(
        type = ProbeType.IQ_PLUS,
        practiceType = PracticeType.HUMAN,
        serialNumber = "PREVIEW-001",
        state = state,
        batteryPercentage = 0.8f,
        batteryState = BatteryState.NORMAL,
        estimatedTemperature = 25f,
        unitRelativeTemperature = 0.0,
        temperatureState = TemperatureState.NORMAL,
        buttonPressedCountMain = 0,
        buttonPressedCountUp = 0,
        buttonPressedCountDown = 0,
        tla = "",
        udiDi = "",
        udiPi = "",
        isEnabled = true,
        isRegistered = true,
        isHealthCheckRequired = false,
    )

internal fun previewImagingState(
    probe: Probe = previewProbe(),
    preset: ImagingPreset? = previewImagingPreset(),
    availablePresets: List<ImagingPreset> = preset?.let { listOf(it) } ?: emptyList(),
) = ImagingState(
    availablePresets = availablePresets,
    bModeImage = null,
    mModeImage = null,
    depth = preset?.defaultDepth ?: 0f,
    depthBounds = preset?.depthBounds ?: Bounds(lower = 0f, upper = 0f),
    gain = preset?.defaultGain ?: 50,
    colorGain = preset?.defaultColorGain ?: 50,
    preset = preset,
    mode = UltrasoundMode.B_MODE,
    flipImageLeftRight = false,
    mechanicalIndex = 0.0,
    thermalIndex = ThermalIndexValues(softTissue = 0.0, bone = 0.0),
    probe = probe,
    imageStartY = 0f,
    bModeImageRect = Rect(x = 0.0, y = 0.0, width = 1.0, height = 1.0),
    bModeVirtualApex = emptyList(),
    bModeHalfFovAngleDeg = 0f,
    dopplerboxMaxAperture = 0f,
    dopplerVirtualApex = emptyList(),
    isAutoFreezeEnabled = false,
    traceImageRect = Rect(x = 0.0, y = 0.0, width = 1.0, height = 1.0),
    colormap = emptyList(),
    defaultColorBox = DefaultColorBoxParameters(
        linearEnabled = false,
        linearX = 0.0,
        linearY = 0.0,
        linearWidth = 0.0,
        linearHeight = 0.0,
        linearAngle = 0.0,
        polarR1 = 0.0,
        polarR2 = 0.0,
        polarAngle = 0.0,
        polarSectorAngle = 0.0,
        polarVirtualApexX = 0.0,
        polarVirtualApexZ = 0.0,
        spectralX = 0.0,
        spectralZ = 0.0,
        spectralSize = 0.0,
        spectralVesselAngle = 0.0,
        spectralBeamStartX = 0.0,
        spectralBeamAngle = 0.0,
        spectralAngleCorrection = 0.0,
    ),
    colorBoxConstraints = ColorBoxConstraintParameters(
        maxAperture = 0f,
        dopplerVirtualApex = PointF(0f, 0f),
        bModeVirtualApex = PointF(0f, 0f),
        bModeHalfFovAngleDeg = 0f,
        minBoxWidthLinear = 0f,
        minBoxHeightLinear = 0f,
        minBoxHeightPolar = 0f,
        minBoxSectorPolar = 0f,
    ),
    flowVelocityScaleMode = FlowVelocityScaleMode.SLOW,
    linearColorBox = LinearColorBox(
        x = 0.0,
        y = 0.0,
        width = 0.0,
        height = 0.0,
        angleDegrees = 0.0,
    ),
    acousticPower = AcousticPower.NORMAL,
    mModeAngle = 0.0,
    mModeAngleMax = 0.0,
    polarColorBox = PolarColorBox(
        r1 = 0.0,
        r2 = 0.0,
        angleDegrees = 0.0,
        sectorAngleDegrees = 0.0,
        virtualApexX = 0.0,
        virtualApexZ = 0.0,
    ),
    tgcFar = 0,
    tgcMid = 0,
    tgcNear = 0,
    traceScrollSpeedMode = TraceScrollSpeedMode.SLOW,
    biplaneSecondaryImage = null,
    biplaneParameters = null,
)
