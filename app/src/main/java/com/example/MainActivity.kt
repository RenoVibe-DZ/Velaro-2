package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VelaroRepository
import com.example.model.UserRole
import com.example.ui.admin.SuperAdminDashboardScreen
import com.example.ui.agency.AgencyDashboardScreen
import com.example.ui.client.ClientHomeScreen
import com.example.ui.components.VelaroHeader
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val appState by VelaroRepository.state.collectAsStateWithLifecycle()

        // Handle system back navigation to return to Client home if in agency or admin
        BackHandler(enabled = appState.currentRole != UserRole.CLIENT) {
          VelaroRepository.switchRole(UserRole.CLIENT)
        }

        Scaffold(
          containerColor = if (appState.currentRole == UserRole.CLIENT) com.example.ui.theme.AluminiumLight else DeepNavy,
          topBar = {
            VelaroHeader(
              currentRole = appState.currentRole,
              onRoleSelected = { role ->
                VelaroRepository.switchRole(role)
              },
              onOpenProfile = {
                VelaroRepository.switchRole(UserRole.CLIENT)
              }
            )
          },
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (appState.currentRole) {
              UserRole.CLIENT -> {
                ClientHomeScreen(appState = appState)
              }
              UserRole.AGENCY -> {
                AgencyDashboardScreen(appState = appState)
              }
              UserRole.SUPER_ADMIN -> {
                SuperAdminDashboardScreen(appState = appState)
              }
            }
          }
        }
      }
    }
  }
}
