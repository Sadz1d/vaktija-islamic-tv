package com.islamictv.admin

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseManager(private val context: Context) {

    // Dynamic reference based on logged-in admin's džemat
    private val contentRef get() = DzamatManager.getContentRef()

    private val cloudinary = CloudinaryManager(context)

    suspend fun uploadImage(uri: Uri): Result<String> {
        return cloudinary.uploadImage(uri)
    }

    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return cloudinary.deleteImage(imageUrl)
    }

    fun getAllContent(): Flow<List<ContentItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<ContentItem>()

                for (childSnapshot in snapshot.children) {
                    try {
                        val item = childSnapshot.getValue(ContentItem::class.java)
                        if (item != null) {
                            items.add(item.copy(id = childSnapshot.key ?: ""))
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseManager", "Error parsing item", e)
                    }
                }

                items.sortByDescending { it.timestamp }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseManager", "Database error: ${error.message}")
            }
        }

        contentRef.addValueEventListener(listener)
        awaitClose { contentRef.removeEventListener(listener) }
    }

    suspend fun addContent(item: ContentItem): Result<String> {
        return try {
            val newRef = contentRef.push()
            val newItem = item.copy(
                id = newRef.key ?: "",
                timestamp = System.currentTimeMillis()
            )
            newRef.setValue(newItem).await()
            Result.success(newRef.key ?: "")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error adding content", e)
            Result.failure(e)
        }
    }

    suspend fun updateContent(item: ContentItem): Result<Unit> {
        return try {
            contentRef.child(item.id).setValue(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error updating content", e)
            Result.failure(e)
        }
    }

    suspend fun deleteContent(id: String): Result<Unit> {
        return try {
            contentRef.child(id).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error deleting content", e)
            Result.failure(e)
        }
    }

    suspend fun permanentlyDeleteContent(id: String): Result<Unit> {
        return try {
            contentRef.child(id).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error permanently deleting content", e)
            Result.failure(e)
        }
    }

    suspend fun toggleActive(id: String, isActive: Boolean): Result<Unit> {
        return try {
            contentRef.child(id).child("active").setValue(isActive).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error toggling active", e)
            Result.failure(e)
        }
    }

    suspend fun getContentById(id: String): ContentItem? {
        return try {
            val snapshot = contentRef.child(id).get().await()
            val item = snapshot.getValue(ContentItem::class.java)
            item?.copy(id = snapshot.key ?: "")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting content by ID", e)
            null
        }
    }
}