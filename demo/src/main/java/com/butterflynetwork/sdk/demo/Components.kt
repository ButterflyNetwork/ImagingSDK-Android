// Copyright 2012-2026 (C) Butterfly Network, Inc.

// Demo app — UI strings are intentionally hardcoded rather than extracted to string resources.

package com.butterflynetwork.sdk.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.butterflynetwork.sdk.demo.ui.theme.ImagingSDKTheme
import com.butterflynetwork.sdk.imaging.model.ImagingPreset
import com.butterflynetwork.sdk.imaging.model.LicenseState
import com.butterflynetwork.sdk.imaging.model.Probe
import com.butterflynetwork.sdk.imaging.model.UltrasoundMode
import kotlin.math.roundToInt

@Composable
internal fun PrimaryActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = text,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
internal fun PresetPicker(
    modifier: Modifier,
    selectedPreset: ImagingPreset?,
    availablePresets: List<ImagingPreset>,
    onPresetChange: (ImagingPreset) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(end = 8.dp),
            text = "Preset:",
        )

        Box(modifier = Modifier.weight(1f)) {
            PrimaryActionButton(
                modifier = Modifier.fillMaxWidth(),
                text = selectedPreset?.displayName ?: "---",
                onClick = { expanded = true },
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                if (selectedPreset == null) {
                    DropdownMenuItem(
                        text = { Text("---") },
                        onClick = { expanded = false },
                    )
                }
                availablePresets.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(text = preset.displayName) },
                        onClick = {
                            onPresetChange(preset)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun BoundedSlider(
    startLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    endLabel: String? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(startLabel)

        Slider(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .padding(horizontal = 8.dp),
            enabled = enabled,
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
        )

        endLabel?.let { Text(text = it) }
    }
}

private val Probe.probeDataList
    get() = listOf(
        "type: $type",
        "practiceType: $practiceType",
        "serialNumber: $serialNumber",
        "state: $state",
        "batteryPercentage: ${(batteryPercentage * 100).roundToInt()}%",
        "batteryState: $batteryState",
        "estimatedTemperature: $estimatedTemperature",
        "unitRelativeTemperature: $unitRelativeTemperature",
        "temperatureState: $temperatureState",
        "buttonPressedCountMain: $buttonPressedCountMain",
        "buttonPressedCountUp: $buttonPressedCountUp",
        "buttonPressedCountDown: $buttonPressedCountDown",
        "tla: $tla",
        "udiDi: $udiDi",
        "udiPi: $udiPi",
        "isEnabled: $isEnabled",
        "isRegistered: $isRegistered",
    )

@Composable
internal fun InfoOverlay(probe: Probe?, licenseState: LicenseState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(Alignment.BottomStart),
    ) {
        probe?.let {
            Text(
                text = it.probeDataList.joinToString("\n"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }

        Text(
            text = "license: $licenseState",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
    }
}

@Composable
internal fun ModePicker(
    selectedMode: UltrasoundMode,
    availableModes: List<UltrasoundMode>,
    onModeChange: (UltrasoundMode) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        availableModes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selectedMode,
                onClick = { onModeChange(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, availableModes.size),
                label = {
                    Text(
                        text = mode.displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
        }
    }
}

private val UltrasoundMode.displayName: String
    get() = when (this) {
        UltrasoundMode.B_MODE -> "B-Mode"
        UltrasoundMode.COLOR_DOPPLER -> "Color Doppler"
        UltrasoundMode.M_MODE -> "M-Mode"
        UltrasoundMode.MULTI_SLICE -> "iQ Slice"
        UltrasoundMode.AUTOFAN -> "iQ Fan"
        UltrasoundMode.BIPLANE -> "Biplane"
    }

private val ImagingPreset.displayName: String get() = if (isVeterinary) "$name (Vet)" else name

@Preview
@Composable
private fun Previews() {
    ImagingSDKTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PrimaryActionButton(text = "Start imaging") {}

                BoundedSlider(
                    startLabel = "Gain: 0",
                    value = 50f,
                    valueRange = 0f..100f,
                    endLabel = "100",
                    onValueChange = {},
                )

                val preset = previewImagingPreset()
                BoundedSlider(
                    startLabel = "Depth: ${(preset.depthBounds.lower * 100).toInt()}cm",
                    value = 0.14f,
                    valueRange = preset.depthBounds.lower..preset.depthBounds.upper,
                    endLabel = "${(preset.depthBounds.upper * 100).toInt()}cm",
                    onValueChange = {},
                )

                ModePicker(
                    selectedMode = UltrasoundMode.B_MODE,
                    availableModes = listOf(
                        UltrasoundMode.B_MODE,
                        UltrasoundMode.COLOR_DOPPLER,
                        UltrasoundMode.M_MODE,
                    ),
                    onModeChange = {},
                )
            }
        }
    }
}
