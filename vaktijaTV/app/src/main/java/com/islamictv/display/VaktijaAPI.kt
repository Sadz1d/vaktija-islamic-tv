package com.islamictv.display

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class VaktijaAPI {

    /**
     * Fetch official prayer times from vaktija.ba API
     * This gives exact IZ BiH times - no calculation needed!
     */
    suspend fun fetchPrayerTimes(): List<PrayerTime>? {
        return withContext(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // 61 = Mostar (ID grada)
                val url = "https://api.vaktija.ba/vaktija/v1/61/$year/$month/$day"

                Log.d("VaktijaAPI", "Calling URL: $url")

                val response = URL(url).readText()
                val json = JSONObject(response)

                val vakatArray = json.getJSONArray("vakat")

                listOf(
                    PrayerTime(
                        arabicName = "الفجر",
                        bosnianName = "Zora",
                        time = vakatArray.getString(0)
                    ),
                    PrayerTime(
                        arabicName = "الشروق",
                        bosnianName = "Izlazak sunca",
                        time = vakatArray.getString(1)
                    ),
                    PrayerTime(
                        arabicName = "الظهر",
                        bosnianName = "Podne",
                        time = vakatArray.getString(2)
                    ),
                    PrayerTime(
                        arabicName = "العصر",
                        bosnianName = "Ikindija",
                        time = vakatArray.getString(3)
                    ),
                    PrayerTime(
                        arabicName = "المغرب",
                        bosnianName = "Akšam",
                        time = vakatArray.getString(4)
                    ),
                    PrayerTime(
                        arabicName = "العشاء",
                        bosnianName = "Jacija",
                        time = vakatArray.getString(5)
                    )
                )

            } catch (e: Exception) {
                Log.e("VaktijaAPI", "API error: ${e.message}", e)
                null
            }
        }
    }

}