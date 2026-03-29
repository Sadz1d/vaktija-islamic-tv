package com.islamictv.display

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.*

class PrayerTimesCalculator {

    /**
     * Calculate accurate prayer times for Mostar, Bosnia
     * Using Islamska Zajednica BiH official parameters
     * Coordinates: 43.3438° N, 17.8078° E (Mostar)
     */
    fun calculatePrayerTimes(
        latitude: Double = 43.3438,  // Mostar
        longitude: Double = 17.8078,  // Mostar
        date: Date = Date()
    ): List<PrayerTime> {

        // IZ BiH uses custom parameters:
        // First parameter: Fajr angle = 18°
        // Second parameter: Isha angle = 17°
        val parameters = CalculationParameters(18.0, 17.0)
        parameters.madhab = Madhab.HANAFI // Bosnia uses Hanafi madhab

        // IZ BiH specific adjustments (fine-tune if needed)
        parameters.adjustments.fajr = 0   // Adjust minutes if needed
        parameters.adjustments.dhuhr = 0
        parameters.adjustments.asr = 0
        parameters.adjustments.maghrib = 0
        parameters.adjustments.isha = 0

        // Create coordinates for Mostar
        val coordinates = Coordinates(latitude, longitude)

        // Get current calendar
        val calendar = Calendar.getInstance()
        calendar.time = date

        val dateComponents = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Calculate prayer times
        val prayerTimes = com.batoulapps.adhan.PrayerTimes(
            coordinates,
            dateComponents,
            parameters
        )

        // Format times in 24-hour format
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return listOf(
            PrayerTime(
                arabicName = "الفجر",
                bosnianName = "Zora",
                time = timeFormat.format(prayerTimes.fajr)
            ),
            PrayerTime(
                arabicName = "الشروق",
                bosnianName = "Izlazak sunca",
                time = timeFormat.format(prayerTimes.sunrise)
            ),
            PrayerTime(
                arabicName = "الظهر",
                bosnianName = "Podne",
                time = timeFormat.format(prayerTimes.dhuhr)
            ),
            PrayerTime(
                arabicName = "العصر",
                bosnianName = "Ikindija",
                time = timeFormat.format(prayerTimes.asr)
            ),
            PrayerTime(
                arabicName = "المغرب",
                bosnianName = "Akšam",
                time = timeFormat.format(prayerTimes.maghrib)
            ),
            PrayerTime(
                arabicName = "العشاء",
                bosnianName = "Jacija",
                time = timeFormat.format(prayerTimes.isha)
            )
        )
    }

    /**
     * Get next prayer name and time
     */
    fun getNextPrayer(
        latitude: Double = 43.3438,
        longitude: Double = 17.8078
    ): Pair<String, String>? {
        val parameters = CalculationParameters(18.0, 17.0)
        parameters.madhab = Madhab.HANAFI

        val coordinates = Coordinates(latitude, longitude)

        val calendar = Calendar.getInstance()
        val dateComponents = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val prayerTimes = com.batoulapps.adhan.PrayerTimes(
            coordinates,
            dateComponents,
            parameters
        )

        val next = prayerTimes.nextPrayer()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return when (next) {
            Prayer.FAJR -> "Zora" to timeFormat.format(prayerTimes.fajr)
            Prayer.DHUHR -> "Podne" to timeFormat.format(prayerTimes.dhuhr)
            Prayer.ASR -> "Ikindija" to timeFormat.format(prayerTimes.asr)
            Prayer.MAGHRIB -> "Akšam" to timeFormat.format(prayerTimes.maghrib)
            Prayer.ISHA -> "Jacija" to timeFormat.format(prayerTimes.isha)
            else -> null
        }
    }
}