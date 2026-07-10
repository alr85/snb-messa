package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.snb.inspect.ApiService
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel

@Composable fun CalCwAdjustmentsMade(viewModel: CalibrationCheckweigherViewModel) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Adjustments Made Placeholder") } }
@Composable fun CalCwDynamicTestAsLeft(viewModel: CalibrationCheckweigherViewModel) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Dynamic Test As Left Placeholder") } }
@Composable fun CalCwStaticTestAsLeft(viewModel: CalibrationCheckweigherViewModel) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Static Test As Left Placeholder") } }
@Composable fun CalCwSummary(viewModel: CalibrationCheckweigherViewModel, apiService: ApiService) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Summary Placeholder") } }
