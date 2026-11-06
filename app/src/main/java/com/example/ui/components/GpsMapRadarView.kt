package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Car
import com.example.model.CarStatus
import com.example.model.GpsTracker
import com.example.ui.theme.*

@Composable
fun GpsMapRadarView(
  cars: List<Car>,
  gpsTrackers: List<GpsTracker>,
  selectedCarId: String?,
  onSelectCar: (String) -> Unit,
  onSimulateTick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedCar = cars.find { it.id == selectedCarId } ?: cars.firstOrNull()
  val tracker = gpsTrackers.find { it.id == selectedCar?.gpsTrackerId }
  var immobilizerActive by remember { mutableStateOf(false) }

  // Animation for pulsing radar beacon
  val infiniteTransition = rememberInfiniteTransition(label = "RadarBeacon")
  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 12f,
    targetValue = 38f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "PulseRadius"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "PulseAlpha"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(DeepNavy)
      .border(1.dp, DeepNavyBorder, RoundedCornerShape(16.dp))
  ) {
    // Header controls for GPS Map
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .background(DeepNavySurface)
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(SuccessGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "CARTE GPS TEMPS RÉEL (VELARO Plus)",
          color = TechCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      FilledTonalButton(
        onClick = onSimulateTick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = ElectricBlue.copy(alpha = 0.2f),
          contentColor = TechCyan
        ),
        modifier = Modifier.testTag("simulate_telemetry_button")
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Télémétrie Direct", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    // Vehicle Quick Filter Chips
    LazyRow(
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.background(DeepNavySurface.copy(alpha = 0.5f))
    ) {
      items(cars) { car ->
        val isSelected = car.id == selectedCar?.id
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isSelected) ElectricBlue else DeepNavy,
          border = BorderStroke(1.dp, if (isSelected) TechCyan else DeepNavyBorder),
          modifier = Modifier.clickable { onSelectCar(car.id) }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                  when (car.status) {
                    CarStatus.AVAILABLE -> SuccessGreen
                    CarStatus.RENTED -> TechCyan
                    CarStatus.MAINTENANCE -> WarningAmber
                  }
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${car.brand} ${car.model.take(8)}",
              fontSize = 11.sp,
              color = if (isSelected) Color.White else TextMuted,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (car.status == CarStatus.RENTED && car.currentSpeedKmh > 0) {
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${car.currentSpeedKmh}km/h",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) TechCyanLight else TechCyan
              )
            }
          }
        }
      }
    }

    // Interactive Radar Canvas
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(230.dp)
        .pointerInput(cars) {
          detectTapGestures { offset ->
            // Tap tolerance detection for vehicle pins
            val width = size.width
            val height = size.height
            cars.forEachIndexed { index, car ->
              val normX = ((car.longitude + 1.5) % 3.0 / 3.0).toFloat().coerceIn(0.1f, 0.9f)
              val normY = ((car.latitude - 34.0) % 3.0 / 3.0).toFloat().coerceIn(0.15f, 0.85f)
              val px = normX * width
              val py = (1f - normY) * height
              val distance = kotlin.math.hypot(offset.x - px, offset.y - py)
              if (distance < 50f) {
                onSelectCar(car.id)
              }
            }
          }
        }
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Dark Mapbox style base
        drawRect(Color(0xFF0A0F1D))

        // Grid lines
        val gridStep = 40f
        var x = 0f
        while (x < w) {
          drawLine(
            color = Color(0xFF1E293B).copy(alpha = 0.4f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
          )
          x += gridStep
        }
        var y = 0f
        while (y < h) {
          drawLine(
            color = Color(0xFF1E293B).copy(alpha = 0.4f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
          )
          y += gridStep
        }

        // Draw styled neon road networks (representing highways RN11, Autoroute Est-Ouest, etc.)
        val roadPath1 = Path().apply {
          moveTo(0f, h * 0.75f)
          cubicTo(w * 0.3f, h * 0.65f, w * 0.6f, h * 0.85f, w, h * 0.45f)
        }
        drawPath(
          path = roadPath1,
          color = Color(0xFF334155),
          style = Stroke(width = 8f)
        )
        drawPath(
          path = roadPath1,
          color = ElectricBlue.copy(alpha = 0.35f),
          style = Stroke(width = 2f)
        )

        val roadPath2 = Path().apply {
          moveTo(w * 0.2f, 0f)
          cubicTo(w * 0.25f, h * 0.4f, w * 0.7f, h * 0.5f, w * 0.85f, h)
        }
        drawPath(
          path = roadPath2,
          color = Color(0xFF1E293B),
          style = Stroke(width = 6f)
        )

        // Trajectory glow for selected car
        selectedCar?.let { car ->
          val normX = ((car.longitude + 1.5) % 3.0 / 3.0).toFloat().coerceIn(0.1f, 0.9f)
          val normY = ((car.latitude - 34.0) % 3.0 / 3.0).toFloat().coerceIn(0.15f, 0.85f)
          val cx = normX * w
          val cy = (1f - normY) * h

          // Historical trailing line
          val trail = Path().apply {
            moveTo(cx - 60f, cy + 30f)
            quadraticTo(cx - 30f, cy + 20f, cx, cy)
          }
          drawPath(
            path = trail,
            brush = Brush.horizontalGradient(
              listOf(Color.Transparent, TechCyan.copy(alpha = 0.8f))
            ),
            style = Stroke(width = 4f)
          )

          // Pulsing radar circles
          drawCircle(
            color = TechCyan.copy(alpha = pulseAlpha),
            radius = pulseRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
          )
          drawCircle(
            color = TechCyan.copy(alpha = pulseAlpha * 0.6f),
            radius = pulseRadius * 1.5f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
          )
        }

        // Draw vehicle markers
        cars.forEach { car ->
          val normX = ((car.longitude + 1.5) % 3.0 / 3.0).toFloat().coerceIn(0.1f, 0.9f)
          val normY = ((car.latitude - 34.0) % 3.0 / 3.0).toFloat().coerceIn(0.15f, 0.85f)
          val px = normX * w
          val py = (1f - normY) * h
          val isCurrent = car.id == selectedCar?.id

          val markerColor = when (car.status) {
            CarStatus.AVAILABLE -> SuccessGreen
            CarStatus.RENTED -> TechCyan
            CarStatus.MAINTENANCE -> WarningAmber
          }

          // Outer halo
          drawCircle(
            color = if (isCurrent) ElectricBlue.copy(alpha = 0.4f) else Color(0x33000000),
            radius = if (isCurrent) 14f else 9f,
            center = Offset(px, py)
          )

          // Core pin
          drawCircle(
            color = markerColor,
            radius = if (isCurrent) 8f else 5.5f,
            center = Offset(px, py)
          )

          // Center white dot
          drawCircle(
            color = Color.White,
            radius = if (isCurrent) 3f else 2f,
            center = Offset(px, py)
          )
        }
      }

      // Compass & Map legend overlay
      Surface(
        color = DeepNavy.copy(alpha = 0.8f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, DeepNavyBorder),
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text("N", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TechCyan)
          Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "Nord",
            tint = TechCyan,
            modifier = Modifier.size(14.dp)
          )
          Text("GPS 4G", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
        }
      }
    }

    // Selected Vehicle Telemetry Drawer / Pop-up Card
    if (selectedCar != null) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(DeepNavySurface)
          .padding(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text(
              text = "${selectedCar.brand} ${selectedCar.model}",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = selectedCar.plateNumber,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TechCyan,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "• ${selectedCar.city}",
                fontSize = 12.sp,
                color = TextMuted
              )
            }
          }

          // Live Speed Badge
          Surface(
            color = if (selectedCar.currentSpeedKmh > 0) ElectricBlue else DeepNavy,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (selectedCar.currentSpeedKmh > 0) TechCyan else DeepNavyBorder),
            modifier = Modifier.padding(start = 8.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${selectedCar.currentSpeedKmh}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color.White
              )
              Text(
                text = "km/h",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TechCyanLight
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Address & GPS telemetry strip
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DeepNavy)
            .padding(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TechCyan,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = selectedCar.address,
            fontSize = 12.sp,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Telemetry KPI Row: Battery, Satellites, Signal, Engine
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text("Boîtier GPS", fontSize = 10.sp, color = TextMuted)
            Text(
              text = tracker?.id ?: "TRK-NON-ASSIGNE",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = TechCyan
            )
          }

          Column {
            Text("Batterie", fontSize = 10.sp, color = TextMuted)
            Text(
              text = "${tracker?.batteryPercent ?: 85}%",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = if ((tracker?.batteryPercent ?: 85) < 30) WarningAmber else SuccessGreen
            )
          }

          Column {
            Text("Satellites", fontSize = 10.sp, color = TextMuted)
            Text(
              text = "${tracker?.satelliteCount ?: 14} sats",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Column {
            Text("Moteur", fontSize = 10.sp, color = TextMuted)
            Text(
              text = if (selectedCar.engineRunning) "En marche" else "Arrêté",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (selectedCar.engineRunning) TechCyan else TextMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security action buttons: Engine cut-off & Track
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = { immobilizerActive = !immobilizerActive },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = if (immobilizerActive) DangerRed else WarningAmber
            ),
            border = BorderStroke(1.dp, if (immobilizerActive) DangerRed else WarningAmber.copy(alpha = 0.5f)),
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = if (immobilizerActive) Icons.Default.Lock else Icons.Default.LockOpen,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (immobilizerActive) "Coupe-circuit ACTIF" else "Coupe-circuit Moteur",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Button(
            onClick = onSimulateTick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricBlue,
              contentColor = Color.White
            ),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Suivi direct", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
