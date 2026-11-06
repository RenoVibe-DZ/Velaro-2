package com.example.model

enum class UserRole(val label: String, val badge: String) {
  CLIENT("Espace Locataire", "Client"),
  AGENCY("Espace Agence Pro", "Loueur Pro"),
  SUPER_ADMIN("Espace Super Admin", "Contrôle Global")
}

enum class AgencyPlan(val label: String, val maxCars: Int, val hasGpsModule: Boolean) {
  MVP("Offre MVP", 10, false),
  VELARO_PLUS("VELARO Plus (Illimité)", Int.MAX_VALUE, true)
}

enum class AgencyStatus(val label: String) {
  APPROVED("Validée"),
  PENDING("En attente de vérification"),
  SUSPENDED("Suspendue")
}

enum class CarStatus(val label: String) {
  AVAILABLE("Disponible"),
  RENTED("En location"),
  MAINTENANCE("En maintenance")
}

enum class TrackerStatus(val label: String) {
  ONLINE("En ligne"),
  WARNING("Alerte / Faible"),
  OFFLINE("Hors ligne")
}

enum class InterventionStatus(val label: String) {
  SCHEDULED("Planifiée"),
  IN_PROGRESS("En cours"),
  COMPLETED("Terminée")
}

enum class BookingStatus(val label: String) {
  CONFIRMED("Confirmée"),
  ACTIVE("En cours"),
  COMPLETED("Clôturée"),
  CANCELLED("Annulée")
}

data class Agency(
  val id: String,
  val name: String,
  val city: String,
  val phone: String,
  val email: String,
  val rcNumber: String, // Registre de commerce
  val nifNumber: String, // NIF fiscal
  val plan: AgencyPlan,
  val status: AgencyStatus,
  val revenueDzd: Long,
  val carCount: Int,
  val address: String
)

data class Car(
  val id: String,
  val agencyId: String,
  val agencyName: String,
  val brand: String,
  val model: String,
  val year: Int,
  val category: String, // Citadine, Berline, SUV, Premium
  val transmission: String, // Manuelle, Automatique
  val fuel: String, // Essence, Diesel, Hybride
  val hasAc: Boolean,
  val seats: Int,
  val pricePerDayDzd: Int,
  val drawableResId: Int,
  val status: CarStatus,
  val mileageKm: Int,
  val city: String,
  val plateNumber: String,
  val gpsTrackerId: String?,
  val currentSpeedKmh: Int,
  val latitude: Double,
  val longitude: Double,
  val address: String,
  val engineRunning: Boolean
)

data class GpsTracker(
  val id: String,
  val imei: String,
  val simNumber: String,
  val carPlate: String,
  val carModel: String,
  val agencyName: String,
  val batteryPercent: Int,
  val signalQualityPercent: Int,
  val satelliteCount: Int,
  val status: TrackerStatus,
  val lastPing: String,
  val motionAlert: Boolean
)

data class GpsIntervention(
  val id: String,
  val trackerId: String,
  val carModel: String,
  val agencyName: String,
  val title: String,
  val description: String,
  val scheduledDate: String,
  val technician: String,
  val status: InterventionStatus
)

data class Booking(
  val id: String,
  val carId: String,
  val carBrandModel: String,
  val agencyName: String,
  val clientName: String,
  val clientPhone: String,
  val city: String,
  val pickupLocation: String,
  val startDate: String,
  val endDate: String,
  val totalDays: Int,
  val totalDzd: Int,
  val status: BookingStatus,
  val createdAt: String
)

data class StaffMember(
  val id: String,
  val name: String,
  val role: String,
  val email: String,
  val phone: String,
  val agencyId: String
)

data class ClientProfile(
  val name: String = "",
  val phone: String = "",
  val email: String = "",
  val hasCniUploaded: Boolean = false,
  val hasPermisUploaded: Boolean = false,
  val cniNumber: String = "",
  val permisNumber: String = ""
)
