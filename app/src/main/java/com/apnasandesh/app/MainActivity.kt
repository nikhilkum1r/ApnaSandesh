package com.apnasandesh.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apnasandesh.app.core.database.AppPreferencesDataStore
import com.apnasandesh.app.core.ui.theme.ApnaSandeshTheme
import com.apnasandesh.app.feature.navigation.ApnaSandeshAppShell
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesDataStore: AppPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by preferencesDataStore.themeMode.collectAsStateWithLifecycle(initialValue = "LIGHT")
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> false
            }
            ApnaSandeshTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApnaSandeshAppShell()
                }
            }
        }
    }
}
