package com.localreach.bizsms

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
import com.localreach.bizsms.core.database.AppPreferencesDataStore
import com.localreach.bizsms.core.ui.theme.BizSmsTheme
import com.localreach.bizsms.feature.navigation.BizSmsAppShell
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
            BizSmsTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BizSmsAppShell()
                }
            }
        }
    }
}
