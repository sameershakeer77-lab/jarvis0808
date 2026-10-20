package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.JarvisViewModel
import com.example.ui.screens.ConsoleScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.MyApplicationTheme

enum class JarvisNavTab(
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  HUD("HUD", Icons.Filled.Psychology, Icons.Outlined.Psychology),
  WORKSPACE("Code", Icons.Filled.Code, Icons.Outlined.Code),
  PLANNER("Plan", Icons.Filled.ListAlt, Icons.Outlined.ListAlt),
  DIAGNOSTICS("Tests", Icons.Filled.Science, Icons.Outlined.Science),
  SETTINGS("Config", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

  private val viewModel: JarvisViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        val context = LocalContext.current
        var currentTab by remember { mutableIntStateOf(0) }

        // Audio permission launcher
        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
          if (isGranted) {
            viewModel.voiceManager.startListening()
          } else {
            Toast.makeText(
              context,
              "Microphone permission is required for voice recognition",
              Toast.LENGTH_LONG
            ).show()
          }
        }

        // Handle Back button to return to HUD tab
        BackHandler(enabled = currentTab != 0) {
          currentTab = 0
        }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          contentWindowInsets = WindowInsets.safeDrawing,
          containerColor = JarvisBackground,
          bottomBar = {
            NavigationBar(
              containerColor = JarvisSurface,
              tonalElevation = 6.dp,
              modifier = Modifier
                .border(width = 1.dp, color = JarvisBorder)
                .navigationBarsPadding()
                .testTag("jarvis_bottom_navigation")
            ) {
              JarvisNavTab.values().forEachIndexed { index, tab ->
                val isSelected = currentTab == index
                NavigationBarItem(
                  selected = isSelected,
                  onClick = { currentTab = index },
                  icon = {
                    Icon(
                      imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                      contentDescription = tab.title,
                      modifier = Modifier.size(20.dp)
                    )
                  },
                  label = {
                    Text(
                      text = tab.title,
                      fontSize = 10.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      fontFamily = FontFamily.Monospace
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = JarvisCyan,
                    selectedTextColor = JarvisCyan,
                    unselectedIconColor = JarvisTextMuted,
                    unselectedTextColor = JarvisTextSecondary,
                    indicatorColor = JarvisSurfaceVariant
                  ),
                  modifier = Modifier.testTag("nav_tab_${tab.title.lowercase()}")
                )
              }
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (currentTab) {
              0 -> ConsoleScreen(viewModel = viewModel)
              1 -> WorkspaceScreen(viewModel = viewModel)
              2 -> PlannerScreen(viewModel = viewModel)
              3 -> DiagnosticsScreen(viewModel = viewModel)
              4 -> SettingsScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}
