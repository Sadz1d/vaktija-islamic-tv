package com.islamictv.display

import android.content.Context

object TVDzamatConfig {

    private const val PREFS_NAME = "tv_config"
    private const val KEY_DZEMAT_ID = "dzemat_id"
    private const val KEY_LOCATION_ID = "location_id"
    private const val KEY_CITY_NAME = "city_name"

    // ---------------------------------------------------------------------------
    // Džemat ID
    // ---------------------------------------------------------------------------

    fun isConfigured(context: Context): Boolean {
        return getDzamijaId(context) != null
    }

    fun saveDzamijaId(context: Context, id: String) {
        prefs(context).edit().putString(KEY_DZEMAT_ID, id.trim()).apply()
    }

    fun getDzamijaId(context: Context): String? {
        val id = prefs(context).getString(KEY_DZEMAT_ID, null)
        return if (id.isNullOrBlank()) null else id
    }

    // ---------------------------------------------------------------------------
    // Grad / lokacija
    // ---------------------------------------------------------------------------

    fun saveLocation(context: Context, locationId: Int, cityName: String) {
        prefs(context).edit()
            .putInt(KEY_LOCATION_ID, locationId)
            .putString(KEY_CITY_NAME, cityName)
            .apply()
    }

    fun getLocationId(context: Context): Int {
        return prefs(context).getInt(KEY_LOCATION_ID, 61) // default: Mostar
    }

    fun getCityName(context: Context): String {
        return prefs(context).getString(KEY_CITY_NAME, "Mostar") ?: "Mostar"
    }

    // ---------------------------------------------------------------------------

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}