package com.example

import com.example.data.VelaroRepository
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `repository initial state is clean and empty for the first user`() {
    val state = VelaroRepository.state.value
    assertNotNull(state)
    assertEquals(UserRole.CLIENT, state.currentRole)
    assertEquals(0, state.cars.size)
    assertEquals(0, state.bookings.size)
    assertEquals("", state.clientProfile.name)
  }

  @Test
  fun `creating first agency and adding car works dynamically`() {
    val agency = VelaroRepository.createAgency(
      name = "Bahia Auto Oran",
      city = "Oran",
      phone = "+213 550 11 22 33",
      email = "contact@bahia-oran.dz",
      rcNumber = "31/00-112233B26",
      nifNumber = "002631000112233",
      plan = AgencyPlan.VELARO_PLUS
    )
    assertNotNull(agency)
    assertEquals("Bahia Auto Oran", agency.name)

    val car = Car(
      id = "car-test-unit",
      agencyId = agency.id,
      agencyName = agency.name,
      brand = "Volkswagen",
      model = "Golf 8",
      year = 2024,
      category = "Berline",
      transmission = "Automatique",
      fuel = "Essence",
      hasAc = true,
      seats = 5,
      pricePerDayDzd = 9500,
      drawableResId = R.drawable.car_clio_modern_1793989986542,
      status = CarStatus.AVAILABLE,
      mileageKm = 24000,
      city = "Oran",
      plateNumber = "01842-124-31",
      gpsTrackerId = "TRK-DZ-TEST",
      currentSpeedKmh = 0,
      latitude = 35.6987,
      longitude = -0.6349,
      address = "Oran Centre",
      engineRunning = false
    )
    val success = VelaroRepository.addCar(car)
    assertEquals(true, success)

    val updatedState = VelaroRepository.state.value
    assert(updatedState.cars.any { it.id == "car-test-unit" })
  }
}
