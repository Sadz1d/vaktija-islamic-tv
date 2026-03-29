package com.islamictv.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddContent: () -> Unit,
    onEditContent: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val firebaseManager = remember { FirebaseManager(context) }
    var contentItems by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
    var showInactive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Džemat name shown in toolbar
    val dzamatNaziv = remember { DzamatManager.getNaziv() }

    // Load content from Firebase
    LaunchedEffect(Unit) {
        firebaseManager.getAllContent().collect { items ->
            contentItems = items
        }
    }

    // Show snackbar
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            dzamatNaziv,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${contentItems.count { it.active }} aktivnih obavještenja",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showInactive = !showInactive }) {
                        Icon(
                            if (showInactive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle inactive",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Odjavi se",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContent,
                containerColor = MaterialTheme.colorScheme.tertiary,

            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val filteredItems = if (showInactive) {
            contentItems
        } else {
            contentItems.filter { it.active }
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LibraryBooks,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nema sadržaja",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Dodajte obavještenje za $dzamatNaziv",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    ContentItemCard(
                        item = item,
                        onEdit = { onEditContent(item.id) },
                        onDelete = {
                            scope.launch {
                                firebaseManager.deleteContent(item.id)
                                snackbarMessage = "Sadržaj obrisan"
                            }
                        },
                        onToggleActive = {
                            scope.launch {
                                firebaseManager.toggleActive(item.id, !item.active)
                                snackbarMessage = if (!item.active) "Aktivirano" else "Deaktivirano"
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentItemCard(
    item: ContentItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.active) Color.White else Color.LightGray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (item.type) {
                            "hadith" -> Icons.Default.Book
                            "verse" -> Icons.Default.MenuBook
                            "announcement" -> Icons.Default.Campaign
                            else -> Icons.Default.Article
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (item.type) {
                            "hadith" -> "Hadis"
                            "verse" -> "Ajet"
                            "announcement" -> "Obavještenje"
                            else -> item.type
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${item.duration}s",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (item.arabicText.isNotEmpty()) {
                Text(
                    item.arabicText.take(100) + if (item.arabicText.length > 100) "..." else "",
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            if (item.bosnianText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    item.bosnianText.take(100) + if (item.bosnianText.length > 100) "..." else "",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }

            if (item.imageUrl.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = "Slika",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                item.reference,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    TextButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Uredi",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uredi")
                    }

                    TextButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Obriši",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Obriši", color = Color.Red)
                    }
                }

                Switch(
                    checked = item.active,
                    onCheckedChange = { onToggleActive() },
                    modifier = Modifier
                        .offset(x = (-120).dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Potvrda brisanja") },
            text = { Text("Da li ste sigurni da želite obrisati ovaj sadržaj?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Obriši", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Otkaži")
                }
            }
        )
    }
}