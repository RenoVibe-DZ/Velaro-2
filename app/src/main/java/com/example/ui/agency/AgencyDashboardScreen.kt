package com.example.ui.agency

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VelaroAppState
import com.example.data.VelaroRepository
import com.example.model.*
import com.example.ui.admin.CreateAgencyDialog
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GpsMapRadarView
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun AgencyDashboardScreen(
  appState: VelaroAppState,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Flotte, 1: Carte GPS, 2: Personnel
  var showAddCarModal by remember { mutableStateOf(false) }
  var showAddStaffModal by remember { mutableStateOf(false) }
  var showCreateAgencyModal by remember { mutableStateOf(false) }
  var addCarError by remember { mutableStateOf<String?>(null) }
  var agencyMenuExpanded by remember { mutableStateOf(false) }

  val activeAgency = appState.agencies.find { it.id == appState.selectedAgencyId }
    ?: appState.agencies.firstOrNull()

  // Onboarding screen if no agency exists yet for the first user
  if (activeAgency == null) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = modifier
        .fillMaxSize()
        .background(DeepNavy)
        .padding(24.dp)
    ) {
      GlassmorphicCard(isDark = true) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(16.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(TechCyan.copy(alpha = 0.15f))
          ) {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = TechCyan, modifier = Modifier.size(36.dp))
          }
          Spacer(Modifier.height(16.dp))
          Text(
            text = "ESPACE AGENCE PRO",
            color = TechCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = "Bienvenue sur VELARO !",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
          )
          Spacer(Modifier.height(8.dp))
          Text(
            text = "Vous êtes le premier utilisateur. Aucune agence n'est encore enregistrée. Créez votre profil d'agence pour commencer à gérer votre flotte et vos traceurs GPS.",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(Modifier.height(20.dp))
          Button(
            onClick = { showCreateAgencyModal = true },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("create_first_agency_button")
          ) {
            Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Créer mon agence", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    if (showCreateAgencyModal) {
      CreateAgencyDialog(
        onDismiss = { showCreateAgencyModal = false },
        onConfirm = { name, city, phone, email, rc, nif, plan, address ->
          VelaroRepository.createAgency(name, city, phone, email, rc, nif, plan, address)
          showCreateAgencyModal = false
        }
      )
    }
    return
  }

  val agencyCars = appState.cars.filter { it.agencyId == activeAgency.id }
  val rentedCount = agencyCars.count { it.status == CarStatus.RENTED }
  val availableCount = agencyCars.count { it.status == CarStatus.AVAILABLE }
  val maintenanceCount = agencyCars.count { it.status == CarStatus.MAINTENANCE }
  val agencyStaff = appState.staffMembers.filter { it.agencyId == activeAgency.id }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DeepNavy)
  ) {
    // Agency Selector & Plan Header
    Surface(
      color = DeepNavySurface,
      border = BorderStroke(0.dp, Color.Transparent),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "ESPACE LOUER PRO (AGENCE)",
              color = TechCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { agencyMenuExpanded = true }
            ) {
              Text(
                text = activeAgency.name,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color.White
              )
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TechCyan
              )
            }
            Text(
              text = "${activeAgency.city} • RC: ${activeAgency.rcNumber}",
              fontSize = 11.sp,
              color = TextMuted
            )
          }

          // Plan Badge
          Surface(
            color = if (activeAgency.plan == AgencyPlan.VELARO_PLUS) TechCyan.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (activeAgency.plan == AgencyPlan.VELARO_PLUS) TechCyan else ElectricBlue)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = if (activeAgency.plan == AgencyPlan.VELARO_PLUS) Icons.Default.GpsFixed else Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = if (activeAgency.plan == AgencyPlan.VELARO_PLUS) TechCyan else ElectricBlue,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = activeAgency.plan.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (activeAgency.plan == AgencyPlan.VELARO_PLUS) TechCyan else Color.White
              )
            }
          }

          // Agency Switcher Dropdown
          DropdownMenu(
            expanded = agencyMenuExpanded,
            onDismissRequest = { agencyMenuExpanded = false },
            modifier = Modifier.background(DeepNavySurface)
          ) {
            appState.agencies.forEach { agency ->
              DropdownMenuItem(
                text = {
                  Column {
                    Text(agency.name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${agency.city} (${agency.plan.label})", color = TextMuted, fontSize = 11.sp)
                  }
                },
                onClick = {
                  VelaroRepository.selectAgency(agency.id)
                  agencyMenuExpanded = false
                }
              )
            }
          }
        }
      }
    }

    // Cockpit Sub-Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = DeepNavy,
      contentColor = TechCyan,
      divider = { HorizontalDivider(color = DeepNavyBorder) },
      modifier = Modifier.fillMaxWidth()
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Flotte (${agencyCars.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag("tab_agency_fleet")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Carte GPS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            if (activeAgency.plan == AgencyPlan.VELARO_PLUS) {
              Spacer(Modifier.width(4.dp))
              Box(Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
            }
          }
        },
        icon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag("tab_agency_gps")
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = { Text("Comptoir (${agencyStaff.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        icon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag("tab_agency_staff")
      )
    }

    when (selectedTab) {
      0 -> {
        // Tab 0: Flotte CRUD & Revenue Dashboard
        LazyColumn(
          contentPadding = PaddingValues(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          // KPI Grid
          item {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              KpiMetricCard(
                title = "Revenus Cumulés",
                value = "${"%,d".format(activeAgency.revenueDzd).replace(',', ' ')}",
                unit = "DZD",
                icon = Icons.Default.AttachMoney,
                accentColor = SuccessGreen,
                isDark = true,
                modifier = Modifier.weight(1f)
              )
              KpiMetricCard(
                title = if (activeAgency.plan == AgencyPlan.MVP) "Véhicules (Max 10)" else "Flotte Illimitée",
                value = "${agencyCars.size}",
                unit = if (activeAgency.plan == AgencyPlan.MVP) "/ 10" else "autos",
                icon = Icons.Default.DirectionsCar,
                accentColor = TechCyan,
                isDark = true,
                modifier = Modifier.weight(1f)
              )
            }
          }

          // Fleet Status Bar
          item {
            GlassmorphicCard(isDark = true) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "État du parc en direct",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  StatusBadge(
                    text = "$rentedCount Louées",
                    color = TechCyan,
                    backgroundColor = DeepNavy
                  )
                  StatusBadge(
                    text = "$availableCount Libres",
                    color = SuccessGreen,
                    backgroundColor = DeepNavy
                  )
                  if (maintenanceCount > 0) {
                    StatusBadge(
                      text = "$maintenanceCount Maint.",
                      color = WarningAmber,
                      backgroundColor = DeepNavy
                    )
                  }
                }
              }
            }
          }

          // Add Car Button & Header
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Véhicules de l'agence",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
              )

              Button(
                onClick = {
                  if (activeAgency.plan == AgencyPlan.MVP && agencyCars.size >= 10) {
                    addCarError = "Limite MVP atteinte (10 voitures max). Basculez sur VELARO Plus dans l'Espace Super Admin."
                  } else {
                    addCarError = null
                    showAddCarModal = true
                  }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.testTag("add_car_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ajouter un véhicule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }

            if (addCarError != null) {
              Spacer(Modifier.height(8.dp))
              Surface(
                color = DangerRed.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DangerRed)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(10.dp)
                ) {
                  Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed)
                  Spacer(Modifier.width(8.dp))
                  Text(addCarError!!, color = Color.White, fontSize = 12.sp)
                }
              }
            }
          }

          // Empty state if no vehicles yet
          if (agencyCars.isEmpty()) {
            item {
              GlassmorphicCard(isDark = true) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                ) {
                  Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TechCyan, modifier = Modifier.size(48.dp))
                  Spacer(Modifier.height(12.dp))
                  Text("Votre flotte est vide", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                  Spacer(Modifier.height(4.dp))
                  Text(
                    text = "Ajoutez votre premier véhicule pour qu'il apparaisse instantanément dans le catalogue locataire et sur la carte GPS.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                  Spacer(Modifier.height(16.dp))
                  Button(
                    onClick = { showAddCarModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ajouter mon premier véhicule", fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }

          // Agency Vehicle Cards
          items(agencyCars) { car ->
            AgencyCarCard(
              car = car,
              onStatusChange = { newStatus ->
                VelaroRepository.updateCarStatus(car.id, newStatus)
              },
              onDelete = {
                VelaroRepository.deleteCar(car.id)
              }
            )
          }
        }
      }

      1 -> {
        // Tab 1: Live Interactive GPS Tracking Map
        if (activeAgency.plan == AgencyPlan.VELARO_PLUS) {
          LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            item {
              GpsMapRadarView(
                cars = agencyCars,
                gpsTrackers = appState.gpsTrackers,
                selectedCarId = appState.selectedGpsCarId,
                onSelectCar = { carId ->
                  VelaroRepository.selectGpsCar(carId)
                },
                onSimulateTick = {
                  VelaroRepository.simulateGpsMovement()
                }
              )
            }

            if (agencyCars.isNotEmpty()) {
              item {
                Text(
                  text = "Télémétrie & Capteurs de la Flotte",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color.White
                )
              }

              items(agencyCars) { car ->
                val tracker = appState.gpsTrackers.find { it.id == car.gpsTrackerId }
                GlassmorphicCard(isDark = true) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column {
                      Text("${car.brand} ${car.model}", fontWeight = FontWeight.Bold, color = Color.White)
                      Text(car.plateNumber, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TechCyan)
                    }
                    Text(
                      text = "${car.currentSpeedKmh} km/h",
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold,
                      color = if (car.currentSpeedKmh > 0) TechCyan else TextMuted
                    )
                  }

                  Spacer(Modifier.height(8.dp))

                  Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("Traceur: ${tracker?.id ?: "Non équipé"}", fontSize = 11.sp, color = TextMuted)
                    Text("Batterie: ${tracker?.batteryPercent ?: 85}%", fontSize = 11.sp, color = SuccessGreen)
                    Text("Kilométrage: ${"%,d".format(car.mileageKm)} km", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                  }
                }
              }
            }
          }
        } else {
          // If agency has only MVP plan, show upgrade banner for GPS module
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp)
          ) {
            Icon(Icons.Default.GpsOff, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text(
              text = "Module GPS réservé à VELARO Plus",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
              text = "Votre agence est actuellement sur l'offre MVP (10 voitures max sans traceurs GPS). Demandez le basculement vers l'offre VELARO Plus au Super Admin pour débloquer la cartographie temps réel, le coupe-circuit et les alertes vitesse.",
              color = TextMuted,
              fontSize = 13.sp
            )
            Spacer(Modifier.height(20.dp))
            Button(
              onClick = {
                // Instantly upgrade agency to VELARO_PLUS for seamless testing!
                VelaroRepository.updateAgencyPlan(activeAgency.id, AgencyPlan.VELARO_PLUS)
              },
              colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = DeepNavy)
            ) {
              Icon(Icons.Default.Bolt, contentDescription = null)
              Spacer(Modifier.width(6.dp))
              Text("Activer VELARO Plus (Simulation)", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      2 -> {
        // Tab 2: Personnel & Counter Staff Management
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
                Text(
                  text = "Personnel & Agents de Comptoir",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = Color.White
                )
                Text(
                  text = "Attribution des accès guichet et contrôle retour",
                  fontSize = 12.sp,
                  color = TextMuted
                )
              }

              Button(
                onClick = { showAddStaffModal = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.testTag("add_staff_button")
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ajouter", fontSize = 12.sp)
              }
            }
          }

          if (agencyStaff.isEmpty()) {
            item {
              GlassmorphicCard(isDark = true) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                ) {
                  Icon(Icons.Default.Badge, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                  Spacer(Modifier.height(10.dp))
                  Text("Aucun agent de comptoir enregistré", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  Spacer(Modifier.height(4.dp))
                  Text(
                    text = "Ajoutez des agents de comptoir pour leur attribuer des accès aux contrats et restitutions.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                  Spacer(Modifier.height(14.dp))
                  Button(
                    onClick = { showAddStaffModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ajouter le premier agent")
                  }
                }
              }
            }
          }

          items(agencyStaff) { staff ->
            GlassmorphicCard(isDark = true) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue)
                ) {
                  Text(
                    text = staff.name.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(staff.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                  Text(staff.role, fontSize = 12.sp, color = TechCyan, fontWeight = FontWeight.SemiBold)
                  Text("${staff.phone} • ${staff.email}", fontSize = 11.sp, color = TextMuted)
                }

                StatusBadge(
                  text = "Actif",
                  color = SuccessGreen,
                  backgroundColor = DeepNavy
                )
              }
            }
          }
        }
      }
    }
  }

  // Add Car Modal
  if (showAddCarModal) {
    AddCarDialog(
      agency = activeAgency,
      onDismiss = { showAddCarModal = false },
      onConfirm = { car ->
        VelaroRepository.addCar(car)
        showAddCarModal = false
      }
    )
  }

  // Add Staff Modal
  if (showAddStaffModal) {
    AddStaffDialog(
      agencyId = activeAgency.id,
      onDismiss = { showAddStaffModal = false },
      onConfirm = { name, role, phone, email ->
        VelaroRepository.addStaffMember(name, role, phone, email, activeAgency.id)
        showAddStaffModal = false
      }
    )
  }
}

@Composable
fun AgencyCarCard(
  car: Car,
  onStatusChange: (CarStatus) -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  GlassmorphicCard(isDark = true, modifier = modifier) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "${car.brand} ${car.model}",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = Color.White
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = car.plateNumber,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TechCyan,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${car.category} • ${car.fuel}",
            fontSize = 12.sp,
            color = TextMuted
          )
        }
      }

      IconButton(onClick = onDelete) {
        Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = TextMuted)
      }
    }

    Spacer(Modifier.height(10.dp))

    // Specs row
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DeepNavy)
        .padding(10.dp)
    ) {
      Column {
        Text("Tarif / Jour", fontSize = 10.sp, color = TextMuted)
        Text(
          text = "${"%,d".format(car.pricePerDayDzd).replace(',', ' ')} DZD",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 13.sp
        )
      }

      Column {
        Text("Compteur", fontSize = 10.sp, color = TextMuted)
        Text(
          text = "${"%,d".format(car.mileageKm)} km",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 13.sp
        )
      }

      Column {
        Text("Boîtier GPS", fontSize = 10.sp, color = TextMuted)
        Text(
          text = car.gpsTrackerId ?: "Aucun",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = if (car.gpsTrackerId != null) TechCyan else TextMuted,
          fontSize = 13.sp
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    // Status switcher chips
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "Changer le statut :",
        fontSize = 11.sp,
        color = TextMuted
      )

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(
          selected = car.status == CarStatus.AVAILABLE,
          onClick = { onStatusChange(CarStatus.AVAILABLE) },
          label = { Text("Libre", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SuccessGreen,
            selectedLabelColor = Color.White,
            containerColor = DeepNavy,
            labelColor = TextMuted
          )
        )

        FilterChip(
          selected = car.status == CarStatus.RENTED,
          onClick = { onStatusChange(CarStatus.RENTED) },
          label = { Text("Louée", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricBlue,
            selectedLabelColor = Color.White,
            containerColor = DeepNavy,
            labelColor = TextMuted
          )
        )

        FilterChip(
          selected = car.status == CarStatus.MAINTENANCE,
          onClick = { onStatusChange(CarStatus.MAINTENANCE) },
          label = { Text("Maint.", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = WarningAmber,
            selectedLabelColor = DeepNavy,
            containerColor = DeepNavy,
            labelColor = TextMuted
          )
        )
      }
    }
  }
}

@Composable
fun AddCarDialog(
  agency: Agency,
  onDismiss: () -> Unit,
  onConfirm: (Car) -> Unit
) {
  var brand by remember { mutableStateOf("") }
  var model by remember { mutableStateOf("") }
  var plate by remember { mutableStateOf("") }
  var priceDzd by remember { mutableStateOf("8000") }
  var category by remember { mutableStateOf("Berline") }
  var transmission by remember { mutableStateOf("Automatique") }
  var fuel by remember { mutableStateOf("Essence") }
  var mileage by remember { mutableStateOf("25000") }
  var selectedImageRes by remember { mutableIntStateOf(R.drawable.car_clio_modern_1793989986542) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Ajouter un véhicule à la flotte", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        OutlinedTextField(
          value = brand,
          onValueChange = { brand = it },
          label = { Text("Marque (ex: Renault, Peugeot, Volkswagen, Hyundai)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = model,
          onValueChange = { model = it },
          label = { Text("Modèle (ex: Clio 5, Golf 8, Tucson)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = plate,
          onValueChange = { plate = it },
          label = { Text("Matricule (ex: 04918-124-31)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = priceDzd,
          onValueChange = { priceDzd = it },
          label = { Text("Prix par jour en DZD") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = mileage,
          onValueChange = { mileage = it },
          label = { Text("Kilométrage actuel") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Photo selector
        Text("Photo du véhicule", fontSize = 12.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = selectedImageRes == R.drawable.car_clio_modern_1793989986542,
            onClick = { selectedImageRes = R.drawable.car_clio_modern_1793989986542 },
            label = { Text("Compacte Blanche", fontSize = 11.sp) }
          )
          FilterChip(
            selected = selectedImageRes == R.drawable.velaro_hero_banner_1793989972705,
            onClick = { selectedImageRes = R.drawable.velaro_hero_banner_1793989972705 },
            label = { Text("Berline / SUV Nuit", fontSize = 11.sp) }
          )
        }

        // Category options
        Text("Catégorie", fontSize = 12.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("Citadine", "Berline", "SUV", "Luxe").forEach { cat ->
            FilterChip(
              selected = category == cat,
              onClick = { category = cat },
              label = { Text(cat, fontSize = 11.sp) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (brand.isNotBlank() && model.isNotBlank()) {
            val newCar = Car(
              id = "car-${System.currentTimeMillis() % 10000}",
              agencyId = agency.id,
              agencyName = agency.name,
              brand = brand.trim(),
              model = model.trim(),
              year = 2024,
              category = category,
              transmission = transmission,
              fuel = fuel,
              hasAc = true,
              seats = 5,
              pricePerDayDzd = priceDzd.toIntOrNull() ?: 8000,
              drawableResId = selectedImageRes,
              status = CarStatus.AVAILABLE,
              mileageKm = mileage.toIntOrNull() ?: 25000,
              city = agency.city,
              plateNumber = if (plate.isNotBlank()) plate.trim() else "0${Random.nextInt(1000, 9999)}-124-${Random.nextInt(10, 48)}",
              gpsTrackerId = if (agency.plan == AgencyPlan.VELARO_PLUS) "TRK-DZ-${Random.nextInt(10, 99)}" else null,
              currentSpeedKmh = 0,
              latitude = 35.6987,
              longitude = -0.6349,
              address = "Agence ${agency.name}, ${agency.city}",
              engineRunning = false
            )
            onConfirm(newCar)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
      ) {
        Text("Enregistrer le véhicule")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Annuler")
      }
    }
  )
}

@Composable
fun AddStaffDialog(
  agencyId: String,
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var role by remember { mutableStateOf("Agent de Comptoir") }
  var phone by remember { mutableStateOf("+213 55") }
  var email by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Nouvel Agent de Comptoir", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nom et Prénom") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = role,
          onValueChange = { role = it },
          label = { Text("Rôle (ex: Accueil, Restitution, Responsable)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Téléphone") },
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
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(name, role, phone, email)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
      ) {
        Text("Ajouter")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Annuler") }
    }
  )
}
