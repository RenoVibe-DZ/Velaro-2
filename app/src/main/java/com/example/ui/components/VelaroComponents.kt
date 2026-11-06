package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun VelaroHeader(
  currentRole: UserRole,
  onRoleSelected: (UserRole) -> Unit,
  onOpenProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showRoleMenu by remember { mutableStateOf(false) }

  Surface(
    color = DeepNavy,
    contentColor = Color.White,
    tonalElevation = 6.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Brand Title with GPS Tech beacon
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { onRoleSelected(UserRole.CLIENT) }
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(ElectricBlue)
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = "Velaro Logo",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "VELARO",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                color = Color.White
              )
              Text(
                text = " CARS",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                color = TechCyan
              )
            }
            Text(
              text = "Plateforme Unifiée • Algérie",
              fontSize = 10.sp,
              color = TextMuted
            )
          }
        }

        // Role Switcher Button
        Box {
          OutlinedButton(
            onClick = { showRoleMenu = true },
            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = DeepNavySurface,
              contentColor = TechCyan
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("role_switcher_button")
          ) {
            Icon(
              imageVector = when (currentRole) {
                UserRole.CLIENT -> Icons.Default.Person
                UserRole.AGENCY -> Icons.Default.Storefront
                UserRole.SUPER_ADMIN -> Icons.Default.Security
              },
              contentDescription = null,
              tint = TechCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currentRole.badge,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = "Changer d'espace",
              tint = TechCyan,
              modifier = Modifier.size(16.dp)
            )
          }

          DropdownMenu(
            expanded = showRoleMenu,
            onDismissRequest = { showRoleMenu = false },
            modifier = Modifier.background(DeepNavySurface)
          ) {
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Person, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(8.dp))
                  Column {
                    Text("Espace Locataire", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Recherche & réservation", fontSize = 11.sp, color = TextMuted)
                  }
                }
              },
              onClick = {
                showRoleMenu = false
                onRoleSelected(UserRole.CLIENT)
              }
            )

            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Storefront, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(8.dp))
                  Column {
                    Text("Espace Agence Pro", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Gestion flotte & GPS", fontSize = 11.sp, color = TextMuted)
                  }
                }
              },
              onClick = {
                showRoleMenu = false
                onRoleSelected(UserRole.AGENCY)
              }
            )

            HorizontalDivider(color = DeepNavyBorder)

            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Security, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(8.dp))
                  Column {
                    Text("Espace Super Admin", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Contrôle global VELARO", fontSize = 11.sp, color = TextMuted)
                  }
                }
              },
              onClick = {
                showRoleMenu = false
                onRoleSelected(UserRole.SUPER_ADMIN)
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun StatusBadge(
  text: String,
  color: Color,
  backgroundColor: Color,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null
) {
  Surface(
    color = backgroundColor,
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
      } else {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
      }
      Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun GlassmorphicCard(
  modifier: Modifier = Modifier,
  isDark: Boolean = false,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  val bgColor = if (isDark) DeepNavySurface else Color.White
  val borderColor = if (isDark) DeepNavyBorder else AluminiumBorder

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = bgColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = if (isDark) 4.dp else 2.dp,
    modifier = modifier.then(
      if (onClick != null) Modifier.clickable { onClick() } else Modifier
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      content = content
    )
  }
}

@Composable
fun KpiMetricCard(
  title: String,
  value: String,
  unit: String = "",
  icon: ImageVector,
  accentColor: Color = TechCyan,
  isDark: Boolean = true,
  modifier: Modifier = Modifier
) {
  GlassmorphicCard(
    modifier = modifier,
    isDark = isDark
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = if (isDark) TextMuted else TextSecondary
      )
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(verticalAlignment = Alignment.Bottom) {
      Text(
        text = value,
        style = KpiMetricStyle,
        color = if (isDark) Color.White else TextPrimary
      )
      if (unit.isNotEmpty()) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = unit,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = accentColor,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(bottom = 2.dp)
        )
      }
    }
  }
}
