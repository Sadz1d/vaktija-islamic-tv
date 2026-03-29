package com.islamictv.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Compress image to reduce file size
 * Target: ~200-300 KB, max 1024x1024 resolution
 */
suspend fun compressImage(context: Context, uri: Uri): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            // Read original image
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            // Calculate new dimensions (max 1024x1024)
            val maxDimension = 1024
            val width = originalBitmap.width
            val height = originalBitmap.height

            val scale = if (width > height) {
                maxDimension.toFloat() / width
            } else {
                maxDimension.toFloat() / height
            }

            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()

            // Resize bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )

            // Compress to JPEG with 85% quality
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

            // Save to temp file
            val tempFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(tempFile)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.close()

            // Clean up
            originalBitmap.recycle()
            resizedBitmap.recycle()

            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    itemId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val firebaseManager = remember { FirebaseManager(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var isCompressingImage by remember { mutableStateOf(false) }
    // Form fields
    var type by remember { mutableStateOf("announcement") }
    var arabicText by remember { mutableStateOf("") }
    var bosnianText by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("15") }
    var active by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingItem by remember { mutableStateOf<ContentItem?>(null) }
// Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Compress image automatically
            scope.launch {
                isCompressingImage = true
                val compressedUri = compressImage(context, uri)
                isCompressingImage = false

                if (compressedUri != null) {
                    selectedImageUri = compressedUri
                    snackbarHostState.showSnackbar("Slika kompresovana")
                } else {
                    snackbarHostState.showSnackbar("Greška pri kompresiji slike")
                }
            }
        }
    }
    // Load existing item if editing
    LaunchedEffect(itemId) {
        if (!itemId.isNullOrEmpty()) {
            isLoading = true
            existingItem = firebaseManager.getContentById(itemId)

            existingItem?.let { item ->
                type = item.type
                arabicText = item.arabicText
                bosnianText = item.bosnianText
                reference = item.reference
                duration = item.duration.toString()
                active = item.active
                imageUrl = item.imageUrl
            }

            isLoading = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId.isNullOrEmpty()) "Dodaj sadržaj" else "Uredi sadržaj") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type selector
                Text("Tip sadržaja", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    /**FilterChip(
                        selected = type == "hadith",
                        onClick = { type = "hadith" },
                        label = { Text("Hadis") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "verse",
                        onClick = { type = "verse" },
                        label = { Text("Ajet") },
                        modifier = Modifier.weight(1f)
                    )**/
                    FilterChip(
                        selected = type == "announcement",
                        onClick = { type = "announcement" },
                        label = { Text("Obavještenje") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Divider()



                // IMAGE UPLOAD SECTION (only for announcements)
                if (type == "announcement") {
                    Text(
                        "Slika",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Show compression status
                    if (isCompressingImage) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kompresovanje slike...", fontSize = 14.sp)
                            }
                        }
                    }

                    // Show existing image or selected image
                    if (selectedImageUri != null || imageUrl.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            Box {
                                AsyncImage(
                                    model = selectedImageUri ?: imageUrl,
                                    contentDescription = "Slika obavještenja",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Remove image button
                                IconButton(
                                    onClick = {
                                        selectedImageUri = null
                                        imageUrl = ""
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Ukloni sliku",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add image button
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dodaj sliku")
                    }

                    Divider()
                }
/**
                // Arabic text
                OutlinedTextField(
                    value = arabicText,
                    onValueChange = { arabicText = it },
                    label = { Text("Arapski tekst") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 8,
                    supportingText = { Text("Opciono - ostavite prazno za obavještenja") }
                )

                // Bosnian text
                OutlinedTextField(
                    value = bosnianText,
                    onValueChange = { bosnianText = it },
                    label = { Text("Bosanski tekst / Prijevod") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 8
                )

                // Reference
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Referenca / Izvor") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("npr. Sahih Bukhari, Kur'an 2:255, Datum...") }
                )
**/
                Divider()

                // Duration
                OutlinedTextField(
                    value = duration,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            duration = it
                        }
                    },
                    label = { Text("Trajanje (sekunde)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Koliko dugo će sadržaj biti prikazan na TV-u") }
                )

                // Active switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text("Aktivno", fontSize = 16.sp)
                        Text(
                            "Ako je aktivno, prikazat će se na TV-u",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = {
                        scope.launch {
                            /**
                            // Validation
                            if (bosnianText.isBlank()) {
                                snackbarHostState.showSnackbar("Bosanski tekst je obavezan!")
                                return@launch
                            }
**/
                            /**if (reference.isBlank()) {
                                snackbarHostState.showSnackbar("Referenca je obavezna!")
                                return@launch
                            }**/
                            if (selectedImageUri == null && imageUrl.isEmpty()) {
                                snackbarHostState.showSnackbar("Slika je obavezna!")
                                return@launch
                            }
                            val durationInt = duration.toIntOrNull() ?: 15
                            if (durationInt < 5 || durationInt > 300) {
                                snackbarHostState.showSnackbar("Trajanje mora biti između 5 i 300 sekundi!")
                                return@launch
                            }

                            isSaving = true

                            // Upload image if selected
                            var finalImageUrl = imageUrl
                            if (selectedImageUri != null) {
                                isUploadingImage = true
                                val uploadResult = firebaseManager.uploadImage(selectedImageUri!!)
                                uploadResult.onSuccess { url ->
                                    finalImageUrl = url
                                    // Delete old image if exists
                                    if (imageUrl.isNotEmpty() && imageUrl != finalImageUrl) {
                                        firebaseManager.deleteImage(imageUrl)
                                    }
                                }.onFailure { error ->
                                    snackbarHostState.showSnackbar("Greška pri uploadu slike: ${error.message}")
                                    isSaving = false
                                    isUploadingImage = false
                                    return@launch
                                }
                                isUploadingImage = false
                            }

                            val item = ContentItem(
                                id = existingItem?.id ?: "",
                                type = type,
                                arabicText = arabicText,
                                bosnianText = bosnianText,
                                reference = reference,
                                duration = durationInt,
                                timestamp = existingItem?.timestamp ?: System.currentTimeMillis(),
                                active = active,
                                imageUrl = finalImageUrl
                            )

                            val result = if (itemId.isNullOrEmpty()) {
                                firebaseManager.addContent(item)
                            } else {
                                firebaseManager.updateContent(item)
                            }

                            isSaving = false

                            result.onSuccess {
                                snackbarHostState.showSnackbar(
                                    if (itemId.isNullOrEmpty()) "Sadržaj dodat!" else "Sadržaj ažuriran!"
                                )
                                kotlinx.coroutines.delay(500)
                                onNavigateBack()
                            }.onFailure { error ->
                                snackbarHostState.showSnackbar("Greška: ${error.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isSaving && !isUploadingImage
                ) {
                    when {
                        isCompressingImage -> Text("Kompresovanje...", fontSize = 16.sp)
                        isUploadingImage -> Text("Upload slike...", fontSize = 16.sp)
                        isSaving -> Text("Snimanje...", fontSize = 16.sp)
                        else -> Text(
                            if (itemId.isNullOrEmpty()) "Dodaj sadržaj" else "Sačuvaj izmjene",
                            fontSize = 16.sp
                        )
                    }
                }

                // Cancel button
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Otkaži")
                }
            }
        }
    }
}