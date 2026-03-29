package com.islamictv.admin

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * Manages which džemat the currently logged-in admin belongs to.
 *
 * Firebase structure:
 *   /admins/{uid}/dzamijaId  →  e.g. "masline"
 *   /admins/{uid}/naziv      →  e.g. "Džemat Mašline"
 *
 * All content is stored under:
 *   /dzamije/{dzamijaId}/content/
 */
object DzamatManager {

    private val database = FirebaseDatabase.getInstance(
        "https://vaktija---masline-default-rtdb.europe-west1.firebasedatabase.app/"
    )

    // Cached after login so we don't re-fetch on every screen
    private var cachedDzamijaId: String? = null
    private var cachedNaziv: String? = null

    /**
     * Call this right after successful login.
     * Reads /admins/{uid} and caches the džemat ID.
     */
    suspend fun loadDzamijaForCurrentUser(): Result<String> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("Korisnik nije prijavljen"))

        return try {
            val snapshot = database.getReference("admins").child(uid).get().await()
            Log.d("TEST", "DATA: ${snapshot.value}")
            Log.d("TEST", "DZAMIJID: ${snapshot.child("dzamijaId").value}")
            val dzamijaId = snapshot.child("dzamijaId").getValue(String::class.java)
                ?: return Result.failure(Exception("Ovaj admin nema dodijeljen džemat. Kontaktirajte podršku."))

            cachedDzamijaId = dzamijaId
            cachedNaziv = snapshot.child("naziv").getValue(String::class.java) ?: dzamijaId

            Log.d("DzamatManager", "Učitan džemat: $dzamijaId")
            Result.success(dzamijaId)
        } catch (e: Exception) {
            Log.e("DzamatManager", "Greška pri učitavanju džemata", e)
            Result.failure(e)
        }
    }

    /**
     * Returns the currently cached džemat ID.
     * Throws if not loaded yet — always call loadDzamijaForCurrentUser() first.
     */
    fun getDzamijaId(): String {
        return cachedDzamijaId
            ?: throw IllegalStateException("DzamatManager nije inicijaliziran. Pozovite loadDzamijaForCurrentUser() nakon prijave.")
    }

    fun getNaziv(): String = cachedNaziv ?: getDzamijaId()

    /**
     * Returns the Firebase reference path for this admin's content.
     * e.g. "dzamije/masline/content"
     */
    fun getContentRef() = database.getReference("dzamije").child(getDzamijaId()).child("content")

    /**
     * Call on sign-out to clear cached data.
     */
    fun clear() {
        cachedDzamijaId = null
        cachedNaziv = null
    }
}