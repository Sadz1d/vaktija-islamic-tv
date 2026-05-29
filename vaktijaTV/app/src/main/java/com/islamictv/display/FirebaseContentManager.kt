package com.islamictv.display

import android.content.Context
import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class ContentItem(
    var id: String = "",
    var type: String = "",
    var arabicText: String = "",
    var bosnianText: String = "",
    var reference: String = "",
    var duration: Long = 15,
    var timestamp: Long = 0,
    var active: Boolean = true,
    var imageUrl: String = ""
)



class FirebaseContentManager(private val dzamijaId: String) {

    private val database = FirebaseDatabase.getInstance(
        "https://vaktija---masline-default-rtdb.europe-west1.firebasedatabase.app/"
    )

    // Reads from /dzamije/{dzamijaId}/content/
    private val contentRef = database
        .getReference("dzamije")
        .child(dzamijaId)
        .child("content")

    fun getActiveContent(): Flow<List<ContentItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<ContentItem>()

                for (childSnapshot in snapshot.children) {
                    try {
                        val item = childSnapshot.getValue(ContentItem::class.java)
                        if (item != null && item.active) {
                            items.add(item.copy(id = childSnapshot.key ?: ""))
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseContent", "Error parsing item", e)
                    }
                }

                items.sortByDescending { it.timestamp }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseContent", "Database error: ${error.message}")
            }
        }

        contentRef.addValueEventListener(listener)
        awaitClose { contentRef.removeEventListener(listener) }
    }
}