package com.islamictv.display

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class VaktijaAPI {

    private val client: OkHttpClient by lazy {
        try {
            // Trust manager koji preskace OCSP provjeru
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }

    suspend fun fetchPrayerTimes(): List<PrayerTime>? {
        return withContext(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val url = "https://api.vaktija.ba/vaktija/v1/61/$year/$month/$day"
                Log.d("VaktijaAPI", "Calling URL: $url")

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (!response.isSuccessful || body == null) {
                    Log.e("VaktijaAPI", "Failed: ${response.code}")
                    return@withContext null
                }

                val json = JSONObject(body)
                val vakatArray = json.getJSONArray("vakat")

                listOf(
                    PrayerTime("الفجر", "Zora", vakatArray.getString(0)),
                    PrayerTime("الشروق", "Izlazak sunca", vakatArray.getString(1)),
                    PrayerTime("الظهر", "Podne", vakatArray.getString(2)),
                    PrayerTime("العصر", "Ikindija", vakatArray.getString(3)),
                    PrayerTime("المغرب", "Akšam", vakatArray.getString(4)),
                    PrayerTime("العشاء", "Jacija", vakatArray.getString(5))
                )

            } catch (e: Exception) {
                Log.e("VaktijaAPI", "API error: ${e.message}", e)
                null
            }
        }
    }
}