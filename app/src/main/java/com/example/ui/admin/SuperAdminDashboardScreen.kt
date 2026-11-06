package com.example.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VelaroAppState
import com.example.data.VelaroRepository
import com.example.model.*
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun SuperAdminDashboardScreen(
  appState: VelaroAppState,
  modifier: Modifier = Modifier
) {
  // If not authenticated as Super Admin, display the official Root Admin Login
  if (!appState.isSuperAdminLoggedIn) {
    SuperAdminLoginView(
      onLoginSuccess = { VelaroRepository.setSuperAdminLoggedIn(true) },
      modifier = modifier
    )
    return
  }

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Statistiques & Agences, 1: Contrats, 2: Parc GPS, 3: Interventions
  var showNewInterventionModal by remember { mutableStateOf(false) }
  var showCreateAgencyModal by remember { mutableStateOf(false) }

  val totalCars = appState.cars.size
  val totalAgencies = appState.agencies.size
  val totalRevenue = appState.agencies.sumOf { it.revenueDzd }
  val activeTrackers = appState.gpsTrackers.count { it.status == TrackerStatus.ONLINE }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DeepNavy)
  ) {
    // Super Admin Top Identity Banner
    Surface(
      color = DeepNavySurface,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(TechCyan.copy(alpha = 0.2f))
          ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = TechCyan, modifier = Modifier.size(20.dp))
          }
          Spacer(Modifier.width(10.dp))
          Column {
            Text("CONTRÔLE GLOBAL VELARO", color = TechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("Super Admin Racine", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
          }
        }

        IconButton(
          onClick = { VelaroRepository.setSuperAdminLoggedIn(false) },
          modifier = Modifier.testTag("admin_logout_button")
        ) {
          Icon(Icons.Default.ExitToApp, contentDescription = "Déconnexion", tint = DangerRed)
        }
      }
    }

    // Navigation Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = DeepNavy,
      contentColor = TechCyan,
      edgePadding = 12.dp,
      divider = { HorizontalDivider(color = DeepNavyBorder) }
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Agences & RC", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_admin_agencies")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Contrats MVP / Plus", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        icon = { Icon(Icons.Default.Subscriptions, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_admin_contracts")
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = { Text("Parc GPS (${appState.gpsTrackers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        icon = { Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_admin_trackers")
      )
      Tab(
        selected = selectedTab == 3,
        onClick = { selectedTab = 3 },
        text = { Text("Interventions (${appState.interventions.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        icon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_admin_interventions")
      )
    }

    when (selectedTab) {
      0 -> {
        // Tab 0: Global Statistics & Agency Verification (RC, NIF)
        LazyColumn(
          contentPadding = PaddingValues(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            // Global Statistics Grid
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              KpiMetricCard(
                title = "Réseau Agences",
                value = "$totalAgencies",
                unit = "agences",
                icon = Icons.Default.Storefront,
                accentColor = TechCyan,
                isDark = true,
                modifier = Modifier.weight(1f)
              )
              KpiMetricCard(
                title = "Total Véhicules",
                value = "$totalCars",
                unit = "voitures",
                icon = Icons.Default.DirectionsCar,
                accentColor = ElectricBlue,
                isDark = true,
                modifier = Modifier.weight(1f)
              )
            }
          }

          item {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              KpiMetricCard(
                title = "Volume d'Affaires",
                value = "${"%,d".format(totalRevenue).replace(',', ' ')}",
                unit = "DZD",
                icon = Icons.Default.MonetizationOn,
                accentColor = SuccessGreen,
                isDark = true,
                modifier = Modifier.weight(1f)
              )
              KpiMetricCard(
                title = "Traceurs Connectés",
                value = "$activeTrackers / ${appState.gpsTrackers.size}",
                unit = "actifs",
                icon = Icons.Default.GpsFixed,
                accentColor = if (activeTrackers == appState.gpsTrackers.size) SuccessGreen else WarningAmber,
                isDark = true,
                modifier = Modifier.weight(1f)
              )
            }
          }

          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Validation & Conformité des Agences",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color.White
                )
                Text(
                  text = "Vérification des pièces : Registre de Commerce (RC) et NIF fiscal",
                  fontSize = 11.sp,
                  color = TextMuted
                )
              }

              Button(
                onClick = { showCreateAgencyModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Inscrire une agence", fontSize = 11.sp)
              }
            }
          }

          if (appState.agencies.isEmpty()) {
            item {
              GlassmorphicCard(isDark = true) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                ) {
                  Icon(Icons.Default.Storefront, contentDescription = null, tint = TechCyan, modifier = Modifier.size(48.dp))
                  Spacer(Modifier.height(10.dp))
                  Text(
                    text = "Aucune agence partenaire enregistrée",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                  )
                  Spacer(Modifier.height(4.dp))
                  Text(
                    text = "Vous êtes le premier utilisateur. Inscrivez votre première agence partenaire pour lancer la plateforme.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                  Spacer(Modifier.height(14.dp))
                  Button(
                    onClick = { showCreateAgencyModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = DeepNavy)
                  ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Inscrire la première agence", fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }

          items(appState.agencies) { agency ->
            GlassmorphicCard(isDark = true) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column {
                  Text(agency.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                  Text("${agency.city} • ${agency.phone}", fontSize = 12.sp, color = TextMuted)
                }

                StatusBadge(
                  text = agency.status.label,
                  color = when (agency.status) {
                    AgencyStatus.APPROVED -> SuccessGreen
                    AgencyStatus.PENDING -> WarningAmber
                    AgencyStatus.SUSPENDED -> DangerRed
                  },
                  backgroundColor = DeepNavy
                )
              }

              Spacer(Modifier.height(10.dp))

              // Documents Grid: RC and NIF
              Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(DeepNavy)
                  .padding(10.dp)
              ) {
                Column {
                  Text("Registre Commerce (RC)", fontSize = 10.sp, color = TextMuted)
                  Text(agency.rcNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TechCyan, fontSize = 12.sp)
                }
                Column {
                  Text("Numéro NIF", fontSize = 10.sp, color = TextMuted)
                  Text(agency.nifNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
                Column {
                  Text("Flotte déclarée", fontSize = 10.sp, color = TextMuted)
                  Text("${agency.carCount} autos", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
              }

              Spacer(Modifier.height(12.dp))

              // Admin Action Buttons
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                if (agency.status != AgencyStatus.APPROVED) {
                  Button(
                    onClick = { VelaroRepository.validateAgency(agency.id, AgencyStatus.APPROVED) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Approuver l'agence", fontSize = 12.sp)
                  }
                }

                if (agency.status == AgencyStatus.APPROVED) {
                  OutlinedButton(
                    onClick = { VelaroRepository.validateAgency(agency.id, AgencyStatus.SUSPENDED) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = BorderStroke(1.dp, DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Suspendre", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }

      1 -> {
        // Tab 1: Gestion des Contrats (MVP vs VELARO Plus)
        LazyColumn(
          contentPadding = PaddingValues(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Text(
              text = "Gestion des Contrats & Abonnements",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = Color.White
            )
            Text(
              text = "Basculez une agence entre l'offre MVP (jusqu'à 10 voitures) et VELARO Plus (flotte illimitée + module GPS live).",
              fontSize = 12.sp,
              color = TextMuted
            )
          }

          items(appState.agencies) { agency ->
            GlassmorphicCard(isDark = true) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column {
                  Text(agency.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                  Text("Actuellement : ${agency.carCount} véhicules", fontSize = 12.sp, color = TextMuted)
                }

                Surface(
                  color = if (agency.plan == AgencyPlan.VELARO_PLUS) TechCyan.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, if (agency.plan == AgencyPlan.VELARO_PLUS) TechCyan else ElectricBlue)
                ) {
                  Text(
                    text = agency.plan.label,
                    color = if (agency.plan == AgencyPlan.VELARO_PLUS) TechCyan else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(Modifier.height(12.dp))

              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                OutlinedButton(
                  onClick = { VelaroRepository.updateAgencyPlan(agency.id, AgencyPlan.MVP) },
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (agency.plan == AgencyPlan.MVP) ElectricBlue.copy(alpha = 0.3f) else Color.Transparent,
                    contentColor = Color.White
                  ),
                  border = BorderStroke(1.dp, if (agency.plan == AgencyPlan.MVP) ElectricBlue else DeepNavyBorder),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text("Activer Offre MVP (Max 10)", fontSize = 11.sp)
                }

                Button(
                  onClick = { VelaroRepository.updateAgencyPlan(agency.id, AgencyPlan.VELARO_PLUS) },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (agency.plan == AgencyPlan.VELARO_PLUS) TechCyan else DeepNavySurface,
                    contentColor = if (agency.plan == AgencyPlan.VELARO_PLUS) DeepNavy else TechCyan
                  ),
                  border = BorderStroke(1.dp, TechCyan),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("VELARO Plus Illimité", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      2 -> {
        // Tab 2: Gestion du Parc GPS
        LazyColumn(
          contentPadding = PaddingValues(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Text(
              text = "Inventaire des Boîtiers GPS & Traceurs",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = Color.White
            )
            Text(
              text = "Suivi de l'état de fonctionnement, batterie et signal des équipements IoT embarqués.",
              fontSize = 12.sp,
              color = TextMuted
            )
          }

          items(appState.gpsTrackers) { tracker ->
            GlassmorphicCard(isDark = true) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column {
                  Text(tracker.id, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = TechCyan, fontSize = 14.sp)
                  Text("IMEI: ${tracker.imei}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
                }

                StatusBadge(
                  text = tracker.status.label,
                  color = when (tracker.status) {
                    TrackerStatus.ONLINE -> SuccessGreen
                    TrackerStatus.WARNING -> WarningAmber
                    TrackerStatus.OFFLINE -> DangerRed
                  },
                  backgroundColor = DeepNavy
                )
              }

              Spacer(Modifier.height(8.dp))

              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                  text = "${tracker.carModel} (${tracker.carPlate}) • ${tracker.agencyName}",
                  fontSize = 12.sp,
                  color = Color.White
                )
              }

              Spacer(Modifier.height(8.dp))

              Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(DeepNavy)
                  .padding(8.dp)
              ) {
                Text("SIM: ${tracker.simNumber}", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                Text("Bat: ${tracker.batteryPercent}%", fontSize = 11.sp, color = if (tracker.batteryPercent < 30) WarningAmber else SuccessGreen, fontFamily = FontFamily.Monospace)
                Text("Ping: ${tracker.lastPing}", fontSize = 11.sp, color = TechCyanLight)
              }
            }
          }
        }
      }

      3 -> {
        // Tab 3: Gestion des Interventions & Maintenance
        LazyColumn(
          contentPadding = PaddingValues(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column {
                Text("Tickets d'Intervention Technique", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Text("Prestations de réparation et maintenance traceurs", fontSize = 12.sp, color = TextMuted)
              }

              Button(
                onClick = { showNewInterventionModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Planifier", fontSize = 12.sp)
              }
            }
          }

          items(appState.interventions) { item ->
            GlassmorphicCard(isDark = true) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(item.id, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TechCyan, fontSize = 13.sp)
                StatusBadge(
                  text = item.status.label,
                  color = when (item.status) {
                    InterventionStatus.SCHEDULED -> ElectricBlue
                    InterventionStatus.IN_PROGRESS -> WarningAmber
                    InterventionStatus.COMPLETED -> SuccessGreen
                  },
                  backgroundColor = DeepNavy
                )
              }

              Spacer(Modifier.height(8.dp))

              Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
              Text(item.description, fontSize = 12.sp, color = TextMuted)

              Spacer(Modifier.height(8.dp))

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Cible: ${item.carModel}", fontSize = 11.sp, color = TechCyanLight)
                Text("Date: ${item.scheduledDate}", fontSize = 11.sp, color = TextMuted)
              }

              Spacer(Modifier.height(4.dp))
              Text("Technicien: ${item.technician}", fontSize = 11.sp, color = Color.White)

              Spacer(Modifier.height(10.dp))

              // Status update actions
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (item.status != InterventionStatus.IN_PROGRESS && item.status != InterventionStatus.COMPLETED) {
                  OutlinedButton(
                    onClick = { VelaroRepository.updateInterventionStatus(item.id, InterventionStatus.IN_PROGRESS) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("Démarrer", fontSize = 11.sp)
                  }
                }
                if (item.status != InterventionStatus.COMPLETED) {
                  Button(
                    onClick = { VelaroRepository.updateInterventionStatus(item.id, InterventionStatus.COMPLETED) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("Marquer Terminée", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Schedule Intervention Dialog
  if (showNewInterventionModal) {
    ScheduleInterventionDialog(
      trackers = appState.gpsTrackers,
      onDismiss = { showNewInterventionModal = false },
      onConfirm = { trackerId, title, desc, date, tech ->
        VelaroRepository.scheduleIntervention(trackerId, title, desc, date, tech)
        showNewInterventionModal = false
      }
    )
  }

  // Create Agency Dialog
  if (showCreateAgencyModal) {
    CreateAgencyDialog(
      onDismiss = { showCreateAgencyModal = false },
      onConfirm = { name, city, phone, email, rc, nif, plan, address ->
        VelaroRepository.createAgency(name, city, phone, email, rc, nif, plan, address)
        showCreateAgencyModal = false
      }
    )
  }
}

@Composable
fun SuperAdminLoginView(
  onLoginSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .fillMaxSize()
      .background(DeepNavy)
      .padding(24.dp)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(TechCyan.copy(alpha = 0.15f))
    ) {
      Icon(Icons.Default.Security, contentDescription = null, tint = TechCyan, modifier = Modifier.size(36.dp))
    }

    Spacer(Modifier.height(16.dp))

    Text("ESPACE 1 : SUPER ADMIN", color = TechCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Text("Contrôle Global VELARO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
    Text(
      text = "Réservé au fondateur pour le pilotage complet de la plateforme.",
      color = TextMuted,
      fontSize = 12.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
    )

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = DeepNavySurface,
      border = BorderStroke(1.dp, DeepNavyBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(20.dp)
      ) {
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Nom d'utilisateur") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_username_input")
        )

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Mot de passe") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_password_input")
        )

        if (error != null) {
          Text(error!!, color = DangerRed, fontSize = 12.sp)
        }

        Button(
          onClick = {
            if (username.trim() == "Velaro" && password.trim() == "21061998") {
              error = null
              onLoginSuccess()
            } else {
              error = "Identifiants incorrects. Veuillez vérifier vos accès."
            }
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_login_submit_button")
        ) {
          Text("Connexion Super Admin", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun ScheduleInterventionDialog(
  trackers: List<GpsTracker>,
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, String, String) -> Unit
) {
  var selectedTrackerId by remember { mutableStateOf(trackers.firstOrNull()?.id ?: "TRK-DZ-01") }
  var title by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var technician by remember { mutableStateOf("Yacine Larbi (Tech Velaro)") }
  var date by remember { mutableStateOf("12 Nov 2026") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Nouvelle Intervention Technique GPS", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Sélectionnez le boîtier GPS cible :", fontSize = 12.sp, color = TextSecondary)
        OutlinedTextField(
          value = selectedTrackerId,
          onValueChange = { selectedTrackerId = it },
          label = { Text("ID Traceur (ex: TRK-DZ-04)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Motif (ex: Remplacement antenne)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Description des travaux") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = technician,
          onValueChange = { technician = it },
          label = { Text("Technicien assigné") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onConfirm(selectedTrackerId, title, desc, date, technician)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
      ) {
        Text("Créer l'intervention")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Annuler") }
    }
  )
}

@Composable
fun CreateAgencyDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, String, String, String, AgencyPlan, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var city by remember { mutableStateOf("Oran") }
  var phone by remember { mutableStateOf("+213 ") }
  var email by remember { mutableStateOf("") }
  var rcNumber by remember { mutableStateOf("") }
  var nifNumber by remember { mutableStateOf("") }
  var plan by remember { mutableStateOf(AgencyPlan.VELARO_PLUS) }
  var address by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AddBusiness, contentDescription = null, tint = TechCyan)
        Spacer(Modifier.width(8.dp))
        Text("Inscrire une Agence Partenaire", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nom de l'agence") },
          placeholder = { Text("ex: Bahia Cars Rental") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = city,
          onValueChange = { city = it },
          label = { Text("Ville") },
          placeholder = { Text("ex: Oran, Alger, Tlemcen...") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Téléphone professionnel") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email professionnel") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = rcNumber,
          onValueChange = { rcNumber = it },
          label = { Text("Registre de Commerce (RC)") },
          placeholder = { Text("ex: 31/00-098234B21") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = nifNumber,
          onValueChange = { nifNumber = it },
          label = { Text("Numéro NIF Fiscal") },
          placeholder = { Text("ex: 002131000982341") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Adresse du siège / comptoir") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Text("Offre & Abonnement", fontSize = 12.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = plan == AgencyPlan.MVP,
            onClick = { plan = AgencyPlan.MVP },
            label = { Text("Offre MVP (Max 10)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = plan == AgencyPlan.VELARO_PLUS,
            onClick = { plan = AgencyPlan.VELARO_PLUS },
            label = { Text("VELARO Plus (Illimité + GPS)", fontSize = 11.sp) }
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(
              name.trim(),
              if (city.isNotBlank()) city.trim() else "Oran",
              phone.trim(),
              email.trim(),
              if (rcNumber.isNotBlank()) rcNumber.trim() else "31/00-${kotlin.random.Random.nextInt(100000, 999999)}B26",
              if (nifNumber.isNotBlank()) nifNumber.trim() else "002631000${kotlin.random.Random.nextInt(100000, 999999)}",
              plan,
              address.trim()
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
      ) {
        Text("Valider et enregistrer", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Annuler") }
    }
  )
}
