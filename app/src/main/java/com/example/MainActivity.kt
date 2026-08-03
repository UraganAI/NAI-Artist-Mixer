package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.PicPickerTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MainViewModel = viewModel()
      val appThemePreset by viewModel.appThemePreset.collectAsStateWithLifecycle()
      val headerColorArgb by viewModel.headerColorArgb.collectAsStateWithLifecycle()
      val paragraphColorArgb by viewModel.paragraphColorArgb.collectAsStateWithLifecycle()
      val warningColorArgb by viewModel.warningColorArgb.collectAsStateWithLifecycle()
      val foregroundColorArgb by viewModel.foregroundColorArgb.collectAsStateWithLifecycle()
      val backgroundColorArgb by viewModel.backgroundColorArgb.collectAsStateWithLifecycle()
      val darkBgColorArgb by viewModel.darkBgColorArgb.collectAsStateWithLifecycle()
      val inputBgColorArgb by viewModel.inputBgColorArgb.collectAsStateWithLifecycle()
      val headingsFont by viewModel.headingsFont.collectAsStateWithLifecycle()
      val defaultFont by viewModel.defaultFont.collectAsStateWithLifecycle()

      PicPickerTheme(
        themePreset = appThemePreset,
        headerColor = androidx.compose.ui.graphics.Color(headerColorArgb),
        paragraphColor = androidx.compose.ui.graphics.Color(paragraphColorArgb),
        foregroundColor = androidx.compose.ui.graphics.Color(foregroundColorArgb),
        backgroundColor = androidx.compose.ui.graphics.Color(backgroundColorArgb),
        darkBgColor = androidx.compose.ui.graphics.Color(darkBgColorArgb),
        inputBgColor = androidx.compose.ui.graphics.Color(inputBgColorArgb),
        headingsFont = headingsFont,
        defaultFont = defaultFont
      ) {
        Surface(color = MaterialTheme.colorScheme.background) {
          MainScreen(viewModel = viewModel)
        }
      }
    }
  }
}
