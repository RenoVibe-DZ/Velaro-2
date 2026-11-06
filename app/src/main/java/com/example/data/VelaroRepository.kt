package com.example.data

import com.example.R
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class VelaroAppState(
  val currentRole: UserRole = UserRole.CLIENT,
  val selectedAgencyId: String = "",
  val isSuperAdminLoggedIn: Boolean = false,
  val isAgencyLoggedIn: Boolean = true,
  val isClientLoggedIn: Boolean = true,
  val agencies: List<Agency> = emptyList(),
  val cars: List<Car> = emptyList(),
  val gpsTrackers: List<GpsTracker> = emptyList(),
  val interventions: List<GpsIntervention> = emptyList(),
  val bookings: List<Booking> = emptyList(),
  val staffMembers: List<StaffMember> = emptyList(),
  val clientProfile: ClientProfile = ClientProfile(),
  val selectedCarForBooking: Car? = null,
  val selectedGpsCarId: String? = null
)

object VelaroRepository {
  private val _state = MutableStateFlow(VelaroAppState())
  val state: StateFlow<VelaroAppState> = _state.asStateFlow()

  fun switchRole(newRole: UserRole) {
    _state.update { it.copy(currentRole = newRole) }
  }

  fun setSuperAdminLoggedIn(loggedIn: Boolean) {
    _state.update { it.copy(isSuperAdminLoggedIn = loggedIn) }
  }

  fun selectAgency(agencyId: String) {
    _state.update { it.copy(selectedAgencyId = agencyId) }
  }

  fun selectGpsCar(carId: String?) {
    _state.update { it.copy(selectedGpsCarId = carId) }
  }

  fun setSelectedCarForBooking(car: Car?) {
    _state.update { it.copy(selectedCarForBooking = car) }
  }

  // --- Agency Creation & Management ---
  fun createAgency(
    name: String,
    city: String,
    phone: String,
    email: String,
    rcNumber: String,
    nifNumber: String,
    plan: AgencyPlan = AgencyPlan.VELARO_PLUS,
    address: String = ""
  ): Agency {
    val newAgency = Agency(
      id = "agency-${System.currentTimeMillis() % 100000}",
      name = name,
      city = city,
      phone = phone,
      email = email,
      rcNumber = rcNumber,
      nifNumber = nifNumber,
      plan = plan,
      status = AgencyStatus.APPROVED,
      revenueDzd = 0L,
      carCount = 0,
      address = if (address.isNotBlank()) address else "Centre-ville, $city"
    )
    _state.update { cur ->
      cur.copy(
        agencies = cur.agencies + newAgency,
        selectedAgencyId = if (cur.selectedAgencyId.isEmpty()) newAgency.id else cur.selectedAgencyId
      )
    }
    return newAgency
  }

  // --- Super Admin Actions ---
  fun validateAgency(agencyId: String, newStatus: AgencyStatus) {
    _state.update { cur ->
      val updated = cur.agencies.map { agency ->
        if (agency.id == agencyId) agency.copy(status = newStatus) else agency
      }
      cur.copy(agencies = updated)
    }
  }

  fun updateAgencyPlan(agencyId: String, newPlan: AgencyPlan) {
    _state.update { cur ->
      val updated = cur.agencies.map { agency ->
        if (agency.id == agencyId) agency.copy(plan = newPlan) else agency
      }
      cur.copy(agencies = updated)
    }
  }

  fun scheduleIntervention(
    trackerId: String,
    title: String,
    description: String,
    scheduledDate: String,
    technician: String
  ) {
    val tracker = _state.value.gpsTrackers.find { it.id == trackerId }
    val newIntervention = GpsIntervention(
      id = "INT-2026-${Random.nextInt(100, 999)}",
      trackerId = trackerId,
      carModel = tracker?.carModel ?: "Véhicule traceur",
      agencyName = tracker?.agencyName ?: "Agence VELARO",
      title = title,
      description = description,
      scheduledDate = scheduledDate,
      technician = technician,
      status = InterventionStatus.SCHEDULED
    )
    _state.update { cur ->
      cur.copy(interventions = listOf(newIntervention) + cur.interventions)
    }
  }

  fun updateInterventionStatus(interventionId: String, newStatus: InterventionStatus) {
    _state.update { cur ->
      val updated = cur.interventions.map { item ->
        if (item.id == interventionId) item.copy(status = newStatus) else item
      }
      cur.copy(interventions = updated)
    }
  }

  // --- Agency Pro Actions ---
  fun addCar(car: Car): Boolean {
    val cur = _state.value
    val agency = cur.agencies.find { it.id == car.agencyId } ?: return false
    val agencyCarsCount = cur.cars.count { it.agencyId == car.agencyId }

    // Enforce MVP limit (max 10)
    if (agency.plan == AgencyPlan.MVP && agencyCarsCount >= 10) {
      return false
    }

    // Auto-create GPS tracker if tracker ID provided or agency on VELARO Plus
    val trackerId = car.gpsTrackerId
    val newTrackers = if (trackerId != null && cur.gpsTrackers.none { it.id == trackerId }) {
      cur.gpsTrackers + GpsTracker(
        id = trackerId,
        imei = "86490204${Random.nextInt(1000000, 9999999)}",
        simNumber = "+213 770 ${Random.nextInt(10, 99)} ${Random.nextInt(10, 99)} ${Random.nextInt(10, 99)}",
        carPlate = car.plateNumber,
        carModel = "${car.brand} ${car.model}",
        agencyName = agency.name,
        batteryPercent = 98,
        signalQualityPercent = 95,
        satelliteCount = 16,
        status = TrackerStatus.ONLINE,
        lastPing = "En direct (5s)",
        motionAlert = false
      )
    } else cur.gpsTrackers

    _state.update { state ->
      val newCars = state.cars + car
      val updatedAgencies = state.agencies.map {
        if (it.id == car.agencyId) it.copy(carCount = it.carCount + 1) else it
      }
      state.copy(
        cars = newCars,
        agencies = updatedAgencies,
        gpsTrackers = newTrackers,
        selectedGpsCarId = state.selectedGpsCarId ?: car.id
      )
    }
    return true
  }

  fun updateCarStatus(carId: String, newStatus: CarStatus) {
    _state.update { cur ->
      val updatedCars = cur.cars.map { car ->
        if (car.id == carId) car.copy(status = newStatus) else car
      }
      cur.copy(cars = updatedCars)
    }
  }

  fun deleteCar(carId: String) {
    _state.update { cur ->
      val deleted = cur.cars.find { it.id == carId }
      val updatedCars = cur.cars.filterNot { it.id == carId }
      val updatedAgencies = cur.agencies.map { agency ->
        if (deleted != null && agency.id == deleted.agencyId) {
          agency.copy(carCount = maxOf(0, agency.carCount - 1))
        } else agency
      }
      cur.copy(cars = updatedCars, agencies = updatedAgencies)
    }
  }

  fun addStaffMember(name: String, role: String, phone: String, email: String, agencyId: String) {
    val member = StaffMember(
      id = "staff-${System.currentTimeMillis() % 10000}",
      name = name,
      role = role,
      phone = phone,
      email = email,
      agencyId = agencyId
    )
    _state.update { cur ->
      cur.copy(staffMembers = cur.staffMembers + member)
    }
  }

  // --- Client Actions ---
  fun updateClientProfile(name: String, phone: String, email: String) {
    _state.update { cur ->
      cur.copy(
        clientProfile = cur.clientProfile.copy(
          name = name,
          phone = phone,
          email = email
        )
      )
    }
  }

  fun updateClientDocuments(hasCni: Boolean, hasPermis: Boolean, cniNum: String, permisNum: String) {
    _state.update { cur ->
      cur.copy(
        clientProfile = cur.clientProfile.copy(
          hasCniUploaded = hasCni,
          hasPermisUploaded = hasPermis,
          cniNumber = cniNum,
          permisNumber = permisNum
        )
      )
    }
  }

  fun createBooking(
    car: Car,
    clientName: String,
    clientPhone: String,
    pickupLocation: String,
    startDate: String,
    endDate: String,
    days: Int
  ): Booking {
    val totalAmount = car.pricePerDayDzd * days
    val newBooking = Booking(
      id = "VEL-2026-DZ${Random.nextInt(100, 999)}",
      carId = car.id,
      carBrandModel = "${car.brand} ${car.model}",
      agencyName = car.agencyName,
      clientName = clientName,
      clientPhone = clientPhone,
      city = car.city,
      pickupLocation = pickupLocation,
      startDate = startDate,
      endDate = endDate,
      totalDays = days,
      totalDzd = totalAmount,
      status = BookingStatus.CONFIRMED,
      createdAt = "Aujourd'hui"
    )

    _state.update { cur ->
      val updatedCars = cur.cars.map { c ->
        if (c.id == car.id) c.copy(status = CarStatus.RENTED) else c
      }
      val updatedAgencies = cur.agencies.map { a ->
        if (a.id == car.agencyId) a.copy(revenueDzd = a.revenueDzd + totalAmount) else a
      }
      cur.copy(
        bookings = listOf(newBooking) + cur.bookings,
        cars = updatedCars,
        agencies = updatedAgencies,
        selectedCarForBooking = null
      )
    }
    return newBooking
  }

  // GPS Simulation: subtle movement and telemetry updates
  fun simulateGpsMovement() {
    _state.update { cur ->
      val updatedCars = cur.cars.map { car ->
        if (car.status == CarStatus.RENTED && car.engineRunning) {
          val speedVariation = Random.nextInt(-4, 6)
          val newSpeed = (car.currentSpeedKmh + speedVariation).coerceIn(20, 110)
          val latDelta = (Random.nextDouble() - 0.5) * 0.0006
          val lngDelta = (Random.nextDouble() - 0.5) * 0.0006
          car.copy(
            currentSpeedKmh = newSpeed,
            latitude = car.latitude + latDelta,
            longitude = car.longitude + lngDelta
          )
        } else car
      }
      cur.copy(cars = updatedCars)
    }
  }
}
