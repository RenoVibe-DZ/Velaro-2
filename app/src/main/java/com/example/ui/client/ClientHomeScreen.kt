package com.example.ui.client

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VelaroAppState
import com.example.data.VelaroRepository
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.Car
import com.example.model.CarStatus
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun ClientHomeScreen(
  appState: VelaroAppState,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Catalogue, 1: Réservations, 2: Profil & CNI
  var selectedCity by remember { mutableStateOf("Toutes") }
  var selectedCategory by remember { mutableStateOf("Toutes") }
  var searchQuery by remember { mutableStateOf("") }
  var showBookingModal by remember { mutableStateOf(false) }
  var selectedCarForBooking by remember { mutableStateOf<Car?>(null) }
  var showDocScanModal by remember { mutableStateOf(false) }

  val cities = listOf("Toutes", "Oran", "Tlemcen", "Alger", "Constantine", "Annaba")
  val categories = listOf("Toutes", "Citadine", "Berline", "SUV", "Luxe / Berline", "Luxe / SUV")

  val filteredCars = remember(appState.cars, selectedCity, selectedCategory, searchQuery) {
    appState.cars.filter { car ->
      val cityMatch = selectedCity == "Toutes" || car.city.equals(selectedCity, ignoreCase = true)
      val catMatch = selectedCategory == "Toutes" || car.category.contains(selectedCategory, ignoreCase = true)
      val queryMatch = searchQuery.isEmpty() ||
          car.brand.contains(searchQuery, ignoreCase = true) ||
          car.model.contains(searchQuery, ignoreCase = true) ||
          car.city.contains(searchQuery, ignoreCase = true)
      cityMatch && catMatch && queryMatch
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AluminiumLight)
  ) {
    // Top Sub-Tabs for Client Experience
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color.White,
      contentColor = ElectricBlue,
      modifier = Modifier.fillMaxWidth()
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Catalogue & Recherche", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag("tab_client_catalog")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Réservations", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            if (appState.bookings.isNotEmpty()) {
              Spacer(Modifier.width(4.dp))
              Badge(containerColor = ElectricBlue) {
                Text("${appState.bookings.size}", color = Color.White)
              }
            }
          }
        },
        icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag("tab_client_bookings")
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = { Text("Profil & CNI", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        icon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.testTag("tab_client_profile")
      )
    }

    when (selectedTab) {
      0 -> {
        // Tab 0: Catalog & Dynamic Search
        LazyColumn(
          contentPadding = PaddingValues(bottom = 24.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          // Hero Banner
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.velaro_hero_banner_1793989972705),
                contentDescription = "Velaro Hero",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        DeepNavy.copy(alpha = 0.5f),
                        DeepNavy.copy(alpha = 0.9f)
                      )
                    )
                  )
              )

              Column(
                modifier = Modifier
                  .align(Alignment.BottomStart)
                  .padding(16.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(TechCyan)
                  )
                  Spacer(Modifier.width(6.dp))
                  Text(
                    text = "LOCATION SÉCURISÉE PAR GPS",
                    color = TechCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                  )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                  text = "Louez votre véhicule partout en Algérie",
                  color = Color.White,
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp
                )
                Text(
                  text = "Oran • Tlemcen • Alger • Constantine • Annaba",
                  color = TextMuted,
                  fontSize = 12.sp
                )
              }
            }
          }

          // Search & Filter Box
          item {
            Surface(
              color = Color.White,
              shadowElevation = 2.dp,
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AluminiumBorder, RoundedCornerShape(16.dp))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                // Search Input
                OutlinedTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  placeholder = { Text("Rechercher marque, modèle ou ville...", fontSize = 13.sp) },
                  leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = ElectricBlue)
                  },
                  trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                      IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                      }
                    }
                  },
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = AluminiumBorder
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_cars_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // City Selector Chips
                Text(
                  text = "Ville de prise en charge",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  items(cities) { city ->
                    val isSelected = selectedCity == city
                    FilterChip(
                      selected = isSelected,
                      onClick = { selectedCity = city },
                      label = { Text(city, fontSize = 12.sp) },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue,
                        selectedLabelColor = Color.White,
                        containerColor = AluminiumCard,
                        labelColor = TextPrimary
                      ),
                      border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) ElectricBlue else AluminiumBorder
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Selector Chips
                Text(
                  text = "Catégorie",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                      selected = isSelected,
                      onClick = { selectedCategory = category },
                      label = { Text(category, fontSize = 12.sp) },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TechCyan,
                        selectedLabelColor = DeepNavy,
                        containerColor = AluminiumCard,
                        labelColor = TextPrimary
                      )
                    )
                  }
                }
              }
            }
          }

          // Catalog Header
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
              Text(
                text = "Véhicules disponibles (${filteredCars.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              if (selectedCity != "Toutes") {
                Text(
                  text = "Filtre: $selectedCity",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = ElectricBlue
                )
              }
            }
          }

          // Car Cards
          items(filteredCars) { car ->
            CarCatalogCard(
              car = car,
              onBookClick = {
                selectedCarForBooking = car
                showBookingModal = true
              },
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }

          if (filteredCars.isEmpty()) {
            item {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DirectionsCar,
                  contentDescription = null,
                  tint = ElectricBlue,
                  modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (appState.cars.isEmpty()) {
                  Text(
                    text = "Bienvenue sur VELARO CARS !",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Vous êtes le premier utilisateur. Aucune annonce n'est encore enregistrée.\nBasculez sur l'Espace Agence pour ajouter vos premiers véhicules.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Button(
                    onClick = { VelaroRepository.switchRole(com.example.model.UserRole.AGENCY) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ouvrir l'Espace Agence Pro")
                  }
                } else {
                  Text(
                    text = "Aucun véhicule trouvé pour ces critères",
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                  )
                  Text(
                    text = "Essayez de sélectionner une autre ville ou catégorie.",
                    fontSize = 12.sp,
                    color = TextMuted
                  )
                }
              }
            }
          }
        }
      }

      1 -> {
        // Tab 1: Bookings History
        BookingsHistoryView(
          bookings = appState.bookings,
          modifier = Modifier.fillMaxSize()
        )
      }

      2 -> {
        // Tab 2: Profile & CNI / Permis de conduire
        ClientProfileView(
          profile = appState.clientProfile,
          onScanClick = { showDocScanModal = true },
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }

  // Booking Modal
  if (showBookingModal && selectedCarForBooking != null) {
    BookingDialog(
      car = selectedCarForBooking!!,
      clientProfile = appState.clientProfile,
      onDismiss = {
        showBookingModal = false
        selectedCarForBooking = null
      },
      onConfirm = { car, name, phone, pickup, start, end, days ->
        VelaroRepository.createBooking(car, name, phone, pickup, start, end, days)
        showBookingModal = false
        selectedCarForBooking = null
        selectedTab = 1 // Switch to Bookings tab to view the confirmed reservation
      }
    )
  }

  // Document Scan Dialog
  if (showDocScanModal) {
    DocumentScanDialog(
      currentProfile = appState.clientProfile,
      onDismiss = { showDocScanModal = false },
      onSave = { cniNum, permisNum ->
        VelaroRepository.updateClientDocuments(true, true, cniNum, permisNum)
        showDocScanModal = false
      }
    )
  }
}

@Composable
fun CarCatalogCard(
  car: Car,
  onBookClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    border = BorderStroke(1.dp, AluminiumBorder),
    shadowElevation = 3.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Column {
      // Car Image with Badges
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
      ) {
        Image(
          painter = painterResource(id = car.drawableResId),
          contentDescription = "${car.brand} ${car.model}",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Gradient at the bottom of the image
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
              )
            )
        )

        // Status Badge Top Left
        Row(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(10.dp)
        ) {
          when (car.status) {
            CarStatus.AVAILABLE -> {
              StatusBadge(
                text = "Disponible",
                color = SuccessGreen,
                backgroundColor = Color.White.copy(alpha = 0.92f)
              )
            }
            CarStatus.RENTED -> {
              StatusBadge(
                text = "En location",
                color = ElectricBlue,
                backgroundColor = Color.White.copy(alpha = 0.92f)
              )
            }
            CarStatus.MAINTENANCE -> {
              StatusBadge(
                text = "En maintenance",
                color = WarningAmber,
                backgroundColor = Color.White.copy(alpha = 0.92f)
              )
            }
          }
        }

        // GPS Tracker Badge Top Right
        if (car.gpsTrackerId != null) {
          Surface(
            color = DeepNavy.copy(alpha = 0.85f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(TechCyan)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "GPS Protégé",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TechCyan
              )
            }
          }
        }

        // City & Agency strip at bottom of photo
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${car.city} • ${car.agencyName}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      // Card Body
      Column(modifier = Modifier.padding(14.dp)) {
        // Title & Category
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text(
              text = "${car.brand} ${car.model}",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = TextPrimary
            )
            Text(
              text = "${car.category} • Année ${car.year}",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }

          // Plate pill
          Surface(
            color = AluminiumCard,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AluminiumBorder)
          ) {
            Text(
              text = car.plateNumber,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Features row: Transmission, Fuel, AC, Seats
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AluminiumCard)
            .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
          CarFeatureItem(
            icon = Icons.Default.Settings,
            text = car.transmission
          )
          CarFeatureItem(
            icon = Icons.Default.LocalGasStation,
            text = car.fuel
          )
          CarFeatureItem(
            icon = Icons.Default.AcUnit,
            text = if (car.hasAc) "Clim A/C" else "Sans Clim"
          )
          CarFeatureItem(
            icon = Icons.Default.People,
            text = "${car.seats} places"
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pricing & Action Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text(
              text = "Tarif par jour",
              fontSize = 11.sp,
              color = TextSecondary
            )
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "${"%,d".format(car.pricePerDayDzd).replace(',', ' ')}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = ElectricBlue
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "DZD/j",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 2.dp)
              )
            }
          }

          // Main CTA Button
          Button(
            onClick = onBookClick,
            enabled = car.status == CarStatus.AVAILABLE,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricBlue,
              disabledContainerColor = AluminiumBorder
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            modifier = Modifier.testTag("book_button_${car.id}")
          ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (car.status == CarStatus.AVAILABLE) "Réserver maintenant" else "Indisponible",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun CarFeatureItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = ElectricBlue,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = TextPrimary
    )
  }
}

@Composable
fun BookingsHistoryView(
  bookings: List<Booking>,
  modifier: Modifier = Modifier
) {
  if (bookings.isEmpty()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = modifier.padding(32.dp)
    ) {
      Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = TextMuted, modifier = Modifier.size(54.dp))
      Spacer(Modifier.height(12.dp))
      Text("Aucune réservation pour l'instant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Text("Consultez notre catalogue pour louer votre prochain véhicule.", color = TextSecondary, fontSize = 13.sp)
    }
  } else {
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = modifier
    ) {
      item {
        Text(
          text = "Historique de vos locations",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = TextPrimary
        )
        Text(
          text = "Présentez votre référence et documents en agence pour récupérer les clés en 2 minutes.",
          fontSize = 12.sp,
          color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
      }

      items(bookings) { booking ->
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White,
          border = BorderStroke(1.dp, AluminiumBorder),
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column {
                Text(
                  text = booking.id,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = ElectricBlue
                )
                Text(
                  text = "Créée le ${booking.createdAt}",
                  fontSize = 11.sp,
                  color = TextMuted
                )
              }

              StatusBadge(
                text = booking.status.label,
                color = when (booking.status) {
                  BookingStatus.CONFIRMED -> TechCyan
                  BookingStatus.ACTIVE -> SuccessGreen
                  BookingStatus.COMPLETED -> TextSecondary
                  BookingStatus.CANCELLED -> DangerRed
                },
                backgroundColor = when (booking.status) {
                  BookingStatus.CONFIRMED -> TechCyanLight
                  BookingStatus.ACTIVE -> SuccessGreenLight
                  BookingStatus.COMPLETED -> AluminiumCard
                  BookingStatus.CANCELLED -> DangerRedLight
                }
              )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = AluminiumBorder)
            Spacer(Modifier.height(10.dp))

            Text(
              text = booking.carBrandModel,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = TextPrimary
            )
            Text(
              text = "Agence : ${booking.agencyName} (${booking.city})",
              fontSize = 12.sp,
              color = TextSecondary
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Place, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(15.dp))
              Spacer(Modifier.width(4.dp))
              Text(
                text = "Prise en charge : ${booking.pickupLocation}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
              )
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DateRange, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(15.dp))
              Spacer(Modifier.width(4.dp))
              Text(
                text = "${booking.startDate} au ${booking.endDate} (${booking.totalDays} jours)",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            Spacer(Modifier.height(12.dp))

            // Total amount strip
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AluminiumCard)
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Text("Total de la location :", fontSize = 12.sp, color = TextSecondary)
              Text(
                text = "${"%,d".format(booking.totalDzd).replace(',', ' ')} DZD",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = ElectricBlue
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ClientProfileView(
  profile: com.example.model.ClientProfile,
  onScanClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showEditProfileModal by remember { mutableStateOf(false) }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    modifier = modifier
  ) {
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Espace Profil & Documents",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextPrimary
          )
          Text(
            text = "Numérisez votre CNI et Permis pour une validation rapide et un retrait sans attente en agence.",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        IconButton(onClick = { showEditProfileModal = true }) {
          Icon(Icons.Default.Edit, contentDescription = "Modifier mes infos", tint = ElectricBlue)
        }
      }
    }

    // User Info Card
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AluminiumBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(16.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(ElectricBlue)
          ) {
            Text(
              text = if (profile.name.isNotBlank()) profile.name.take(2).uppercase() else "DZ",
              fontWeight = FontWeight.Black,
              fontSize = 18.sp,
              color = Color.White
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (profile.name.isNotBlank()) profile.name else "Nouveau Locataire",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = TextPrimary
            )
            Text(
              text = if (profile.phone.isNotBlank()) profile.phone else "Téléphone non renseigné",
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              color = TextSecondary
            )
            Text(
              text = if (profile.email.isNotBlank()) profile.email else "Touchez 'Modifier' pour renseigner vos coordonnées",
              fontSize = 12.sp,
              color = TextMuted
            )
          }

          TextButton(onClick = { showEditProfileModal = true }) {
            Text("Modifier", fontSize = 12.sp, color = ElectricBlue)
          }
        }
      }
    }

    // Documents Status Card
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AluminiumBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Documents Officiels",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = TextPrimary
            )

            if (profile.hasCniUploaded && profile.hasPermisUploaded) {
              StatusBadge(
                text = "Vérifié & Prêt",
                color = SuccessGreen,
                backgroundColor = SuccessGreenLight,
                icon = Icons.Default.CheckCircle
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // CNI item
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(AluminiumCard)
              .padding(12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Badge, contentDescription = null, tint = ElectricBlue)
              Spacer(Modifier.width(10.dp))
              Column {
                Text("Carte Nationale d'Identité (CNI)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(
                  text = if (profile.hasCniUploaded) "N° ${profile.cniNumber}" else "Non téléchargée",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = if (profile.hasCniUploaded) SuccessGreen else DangerRed
                )
              }
            }

            Icon(
              imageVector = if (profile.hasCniUploaded) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (profile.hasCniUploaded) SuccessGreen else DangerRed,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Permis item
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(AluminiumCard)
              .padding(12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DriveEta, contentDescription = null, tint = ElectricBlue)
              Spacer(Modifier.width(10.dp))
              Column {
                Text("Permis de Conduire (Catégorie B)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(
                  text = if (profile.hasPermisUploaded) "N° ${profile.permisNumber}" else "Non téléchargé",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = if (profile.hasPermisUploaded) SuccessGreen else DangerRed
                )
              }
            }

            Icon(
              imageVector = if (profile.hasPermisUploaded) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (profile.hasPermisUploaded) SuccessGreen else DangerRed,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedButton(
            onClick = onScanClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = ElectricBlue
            ),
            border = BorderStroke(1.dp, ElectricBlue),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("scan_documents_button")
          ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Numériser / Mettre à jour les documents", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  }

  if (showEditProfileModal) {
    EditProfileDialog(
      currentProfile = profile,
      onDismiss = { showEditProfileModal = false },
      onSave = { name, phone, email ->
        VelaroRepository.updateClientProfile(name, phone, email)
        showEditProfileModal = false
      }
    )
  }
}

@Composable
fun EditProfileDialog(
  currentProfile: com.example.model.ClientProfile,
  onDismiss: () -> Unit,
  onSave: (String, String, String) -> Unit
) {
  var name by remember { mutableStateOf(currentProfile.name) }
  var phone by remember { mutableStateOf(currentProfile.phone) }
  var email by remember { mutableStateOf(currentProfile.email) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Mes Coordonnées", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nom et Prénom") },
          placeholder = { Text("ex: Karim Benali") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Numéro de Téléphone") },
          placeholder = { Text("ex: +213 550 12 34 56") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Adresse Email") },
          placeholder = { Text("ex: karim@gmail.com") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(name.trim(), phone.trim(), email.trim()) },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
      ) {
        Text("Enregistrer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Annuler") }
    }
  )
}

@Composable
fun BookingDialog(
  car: Car,
  clientProfile: com.example.model.ClientProfile,
  onDismiss: () -> Unit,
  onConfirm: (Car, String, String, String, String, String, Int) -> Unit
) {
  var durationDays by remember { mutableIntStateOf(3) }
  var pickupLocation by remember { mutableStateOf("Aéroport (${car.city})") }
  var includeInsurance by remember { mutableStateOf(true) }
  var clientName by remember { mutableStateOf(clientProfile.name) }
  var clientPhone by remember { mutableStateOf(clientProfile.phone) }

  val insuranceRatePerDay = 1500
  val totalRental = (car.pricePerDayDzd + (if (includeInsurance) insuranceRatePerDay else 0)) * durationDays

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text("Réservation Véhicule", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DeepNavy)
        Text("${car.brand} ${car.model} (${car.city})", fontSize = 13.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Durée selector
        Text("Durée de la location", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(1, 2, 3, 5, 7, 14).forEach { days ->
            FilterChip(
              selected = durationDays == days,
              onClick = { durationDays = days },
              label = { Text("$days j", fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ElectricBlue,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        // Lieu de prise en charge
        Text("Lieu de prise en charge", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        val locations = listOf(
          "Aéroport (${car.city})",
          "Agence Centre-Ville",
          "Livraison Hôtel / Domicile"
        )
        locations.forEach { loc ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (pickupLocation == loc) TechCyanLight else AluminiumCard,
            border = BorderStroke(1.dp, if (pickupLocation == loc) TechCyan else AluminiumBorder),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { pickupLocation = loc }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(10.dp)
            ) {
              RadioButton(
                selected = pickupLocation == loc,
                onClick = { pickupLocation = loc },
                colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue)
              )
              Spacer(Modifier.width(6.dp))
              Text(loc, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
          }
        }

        // Insurance checkbox
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = AluminiumCard,
          border = BorderStroke(1.dp, AluminiumBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .padding(10.dp)
              .clickable { includeInsurance = !includeInsurance }
          ) {
            Checkbox(
              checked = includeInsurance,
              onCheckedChange = { includeInsurance = it },
              colors = CheckboxDefaults.colors(checkedColor = ElectricBlue)
            )
            Spacer(Modifier.width(6.dp))
            Column {
              Text("Pack VELARO Zen (+1 500 DZD/j)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text("Zéro franchise • Assistance panne & dépannage 24/7", fontSize = 11.sp, color = TextSecondary)
            }
          }
        }

        // Customer details
        OutlinedTextField(
          value = clientName,
          onValueChange = { clientName = it },
          label = { Text("Nom et Prénom") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = clientPhone,
          onValueChange = { clientPhone = it },
          label = { Text("Numéro de Téléphone") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Total breakdown
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = DeepNavySurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Prix de base ($durationDays j) :", color = TextMuted, fontSize = 12.sp)
              Text("${"%,d".format(car.pricePerDayDzd * durationDays).replace(',', ' ')} DZD", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
            if (includeInsurance) {
              Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Assurance Zen ($durationDays j) :", color = TextMuted, fontSize = 12.sp)
                Text("${"%,d".format(insuranceRatePerDay * durationDays).replace(',', ' ')} DZD", color = TechCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
              }
            }
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = DeepNavyBorder)
            Spacer(Modifier.height(6.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("TOTAL ESTIMÉ :", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(
                text = "${"%,d".format(totalRental).replace(',', ' ')} DZD",
                color = TechCyan,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(car, clientName, clientPhone, pickupLocation, "06 Nov 2026", "09 Nov 2026", durationDays)
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
        modifier = Modifier.testTag("confirm_booking_button")
      ) {
        Text("Confirmer la réservation", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Annuler", color = TextSecondary)
      }
    }
  )
}

@Composable
fun DocumentScanDialog(
  currentProfile: com.example.model.ClientProfile,
  onDismiss: () -> Unit,
  onSave: (String, String) -> Unit
) {
  var cniNum by remember { mutableStateOf(currentProfile.cniNumber) }
  var permisNum by remember { mutableStateOf(currentProfile.permisNumber) }
  var scanSimulated by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = ElectricBlue)
        Spacer(Modifier.width(8.dp))
        Text("Numérisation CNI & Permis", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Capturez ou entrez les numéros de vos pièces d'identité pour préparer votre contrat de location à l'avance.",
          fontSize = 12.sp,
          color = TextSecondary
        )

        OutlinedTextField(
          value = cniNum,
          onValueChange = { cniNum = it },
          label = { Text("Numéro CNI (Biométrique)") },
          leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = permisNum,
          onValueChange = { permisNum = it },
          label = { Text("Numéro de Permis de Conduire") },
          leadingIcon = { Icon(Icons.Default.DriveEta, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Button(
          onClick = {
            scanSimulated = true
            cniNum = "11894200491823"
            permisNum = "DZ-00192847-TLEM"
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = DeepNavy),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Scanner avec la Caméra (OCR Auto)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (scanSimulated) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SuccessGreenLight,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(8.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text("Données extraites avec succès !", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(cniNum, permisNum) },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
      ) {
        Text("Enregistrer les documents")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Fermer")
      }
    }
  )
}
