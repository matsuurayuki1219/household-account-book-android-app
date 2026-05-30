package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.ui.BudgetViewModel
import com.example.ui.BudgetViewModelFactory
import com.example.ui.components.MainScreen
import com.example.ui.theme.KakeiboTheme

class MainActivity : ComponentActivity() {
  private val viewModel: BudgetViewModel by viewModels {
    BudgetViewModelFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val systemTheme = isSystemInDarkTheme()
      var isDarkMode by remember { mutableStateOf(systemTheme) }

      KakeiboTheme(darkTheme = isDarkMode) {
        MainScreen(
          viewModel = viewModel,
          isDark = isDarkMode,
          onToggleDark = { isDarkMode = !isDarkMode }
        )
      }
    }
  }
}
