package com.playflix.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.playflix.player.core.design.AppThemeMode
import com.playflix.player.core.design.PlayFlixTheme
import com.playflix.player.core.permissions.PermissionHelper
import com.playflix.player.feature.home.HomeViewModel
import com.playflix.player.navigation.PlayFlixRootScreen

class MainActivity : ComponentActivity() {

    private val app: PlayFlixApplication
        get() = application as PlayFlixApplication

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(
            scanner = app.mediaScanner,
            dao = app.database.dao(),
            playerController = app.playerController
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(AppThemeMode.DARK) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionsMap ->
                val allGranted = permissionsMap.values.all { it }
                homeViewModel.onPermissionStatusChanged(allGranted)
            }

            LaunchedEffect(Unit) {
                val alreadyGranted = PermissionHelper.hasMediaPermissions(this@MainActivity)
                if (alreadyGranted) {
                    homeViewModel.onPermissionStatusChanged(true)
                } else {
                    permissionLauncher.launch(PermissionHelper.requiredMediaPermissions())
                }
            }

            PlayFlixTheme(themeMode = themeMode) {
                PlayFlixRootScreen(
                    viewModel = homeViewModel,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it },
                    onRequestPermissions = {
                        permissionLauncher.launch(PermissionHelper.requiredMediaPermissions())
                    }
                )
            }
        }
    }
}
