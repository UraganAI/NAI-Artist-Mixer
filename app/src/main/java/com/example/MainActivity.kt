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
      val customPrimaryArgb by viewModel.customPrimaryColorArgb.collectAsStateWithLifecycle()
      val customBgArgb by viewModel.customBgColorArgb.collectAsStateWithLifecycle()

      PicPickerTheme(
        themePreset = appThemePreset,
        customPrimaryColor = androidx.compose.ui.graphics.Color(customPrimaryArgb),
        customBgColor = androidx.compose.ui.graphics.Color(customBgArgb)
      ) {
        Surface(color = MaterialTheme.colorScheme.background) {
          MainScreen(viewModel = viewModel)
        }
      }
    }
  }
}
