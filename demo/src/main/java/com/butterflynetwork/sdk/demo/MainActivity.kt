// Copyright 2012-2026 (C) Butterfly Network, Inc.

package com.butterflynetwork.sdk.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.butterflynetwork.sdk.demo.ui.theme.ImagingSDKTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val viewModel: ImagingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        setContent {
            ImagingSDKTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                ) {
                    ImagingScreen(viewModel)
                }
            }
        }

        viewModel.startup()
    }

    override fun onStop() {
        viewModel.onAppBackground()
        super.onStop()
    }
}
