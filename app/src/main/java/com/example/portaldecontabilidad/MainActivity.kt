package com.example.portaldecontabilidad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.portaldecontabilidad.ui.AppNavigation
import com.example.portaldecontabilidad.ui.theme.PortalDeContabilidadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortalDeContabilidadTheme {
                AppNavigation()
            }
        }
    }
}
