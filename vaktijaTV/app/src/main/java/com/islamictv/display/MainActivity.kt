package com.islamictv.display

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.islamictv.display.ui.theme.IslamicTVDisplayTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import coil.ImageLoader
import coil.ImageLoaderFactory

class MainActivity : ComponentActivity(), ImageLoaderFactory {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            IslamicTVDisplayTheme {
                TVApp()
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return CoilConfig.getImageLoader(this)
    }
}

/**
 * Root composable:
 * - Shows setup screen on first launch (enter džemat ID once)
 * - Shows main display after that
 */
@Composable
fun TVApp() {
    val context = LocalContext.current
    var isConfigured by remember { mutableStateOf(TVDzamatConfig.isConfigured(context)) }

    if (!isConfigured) {
        TVSetupScreen(
            onConfigured = { isConfigured = true }
        )
    } else {
        SplitScreenDisplay()
    }
}

/**
 * One-time setup screen shown only on first launch.
 * Admin enters the džemat ID (e.g. "masline") and it's saved locally.
 */
@Composable
fun TVSetupScreen(onConfigured: () -> Unit) {
    val context = LocalContext.current
    var dzamijaId by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B4332)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D6A4F))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Postavljanje TV ekrana",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    "Unesite ID vašeg džemata.\nOvo se radi samo jednom.",
                    fontSize = 18.sp,
                    color = Color(0xFFB7E4C7),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dzamijaId,
                    onValueChange = {
                        dzamijaId = it
                        error = null
                    },
                    label = { Text("ID džemata", color = Color(0xFFB7E4C7)) },
                    placeholder = { Text("npr. dzemat123", color = Color(0xFF74C69D)) },
                    supportingText = {
                        Text(
                            "ID dobijate od administratora sistema pri postavljanju.",
                            color = Color(0xFF74C69D),
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF74C69D),
                        unfocusedBorderColor = Color(0xFF40916C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error!!, color = Color(0xFFFFE66D), fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        if (dzamijaId.isBlank()) {
                            error = "ID džemata ne može biti prazan"
                        } else {
                            TVDzamatConfig.saveDzamijaId(context, dzamijaId)
                            onConfigured()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF40916C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Potvrdi", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SplitScreenDisplay() {
    val context = LocalContext.current
    var hasContent by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B4332))
    ) {
        PrayerTimesPanel(
            modifier = Modifier
                .weight(if (hasContent) 1f else 2f)
                .fillMaxHeight(),
            isFullScreen = !hasContent
        )

        if (hasContent) {
            IslamicContentPanel(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onContentAvailable = { available -> hasContent = available }
            )
        } else {
            IslamicContentPanel(
                modifier = Modifier.size(0.dp),
                onContentAvailable = { available -> hasContent = available }
            )
        }
    }
}

@Composable
fun PrayerTimesPanel(modifier: Modifier = Modifier, isFullScreen: Boolean = false) {
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    var currentDate by remember { mutableStateOf(getCurrentDateBosnian()) }
    var prayerTimes by remember { mutableStateOf<List<PrayerTime>>(emptyList()) }
    var nextPrayer by remember { mutableStateOf<PrayerTime?>(null) }
    var countdownText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(prayerTimes) {
        if (prayerTimes.isEmpty()) return@LaunchedEffect
        while (true) {
            val now = Calendar.getInstance()
            val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            var foundNext: PrayerTime? = null

            for (i in prayerTimes.indices) {
                val current = prayerTimes[i]
                val next = if (i < prayerTimes.size - 1) prayerTimes[i + 1] else prayerTimes[0]
                val prayerMinutes = getTimeInMinutes(current.time)
                val nextMinutes = getTimeInMinutes(next.time)
                val isCurrentPrayer = if (nextMinutes > prayerMinutes) {
                    currentMinutes >= prayerMinutes && currentMinutes < nextMinutes
                } else {
                    currentMinutes < nextMinutes
                }
                if (isCurrentPrayer) {
                    foundNext = next
                    val remainingMinutes = nextMinutes - currentMinutes
                    val hours = remainingMinutes / 60
                    val minutes = (remainingMinutes % 60) - 1
                    val seconds = 59 - now.get(Calendar.SECOND)
                    countdownText = "Za: %02d:%02d:%02d".format(hours, minutes, seconds)
                    break
                }
            }
            nextPrayer = foundNext
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        val api = VaktijaAPI()
        var lastSuccessfulFetch: Long = 0
        while (true) {
            val todayMidnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            if (lastSuccessfulFetch < todayMidnight) {
                try {
                    val newTimes = api.fetchPrayerTimes()
                    if (newTimes != null) {
                        prayerTimes = newTimes
                        lastSuccessfulFetch = System.currentTimeMillis()
                        Log.d("PrayerTimes", "✅ API SUCCESS!! Times updated")
                    }
                } catch (e: Exception) {
                    Log.e("PrayerTimes", "❌ Exception: ${e.message}")
                }
            }
            delay(30_000L)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = getCurrentTime()
            currentDate = getCurrentDateBosnian()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val maxScroll = scrollState.maxValue
            if (maxScroll <= 0) { delay(1000); continue }
            delay(2000)
            scrollState.animateScrollTo(maxScroll, animationSpec = tween(12_000, easing = LinearEasing))
            delay(2000)
            scrollState.animateScrollTo(0, animationSpec = tween(1500, easing = LinearEasing))
        }
    }

    Column(modifier = modifier.background(Color(0xFF2D6A4F)).padding(24.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("مواقيت الصلاة", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Vaktija - Mostar", fontSize = 28.sp, fontWeight = FontWeight.Medium, color = Color(0xFFB7E4C7))
            Text(currentTime, fontSize = 60.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD8F3DC))
            Text(currentDate, fontSize = 25.sp, color = Color(0xFFB7E4C7))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (prayerTimes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Učitavanje vaktije...", fontSize = 24.sp, color = Color(0xFFFFE66D))
            }
        } else if (isFullScreen) {
            Column(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val rows = prayerTimes.chunked(2)
                rows.forEach { rowPrayers ->
                    Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowPrayers.forEach { prayer ->
                            PrayerTimeRow(
                                prayer = prayer,
                                showCountdown = prayer == nextPrayer,
                                countdownText = countdownText,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                        if (rowPrayers.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                prayerTimes.forEach { prayer ->
                    PrayerTimeRow(prayer = prayer, showCountdown = prayer == nextPrayer, countdownText = countdownText)
                }
            }
        }
    }
}

@Composable
fun PrayerTimeRow(
    prayer: PrayerTime,
    showCountdown: Boolean,
    countdownText: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (showCountdown) Color(0xFF38A169) else Color(0xFF40916C)
    val borderColor = if (showCountdown) Color(0xFF276749) else Color.Transparent
    val verticalPadding = if (showCountdown) 8.dp else 16.dp

    Card(
        modifier = modifier.fillMaxWidth().border(3.dp, borderColor, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                //modifier = if (showCountdown) Modifier.padding(top = 4.dp) else Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(prayer.arabicName, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("  |  ", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD8F3DC))
                Text(prayer.bosnianName, fontSize = 24.sp, color = Color(0xFFD8F3DC))
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(prayer.time, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE66D))
                if (showCountdown) {
                    Text(countdownText, fontSize = 18.sp, color = Color(0xFFFFE66D))
                }
            }
        }
    }
}

@Composable
fun IslamicContentPanel(
    modifier: Modifier = Modifier,
    onContentAvailable: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(0) }
    var contentItems by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
    LaunchedEffect(contentItems) {
        currentIndex = 0
    }
    // Get the džemat ID saved during setup
    val dzamijaId = remember { TVDzamatConfig.getDzamijaId(context) ?: "" }
    val firebaseManager = remember { FirebaseContentManager(dzamijaId) }

    LaunchedEffect(Unit) {
        firebaseManager.getActiveContent().collect { items ->
            if (items.isNotEmpty()) {
                contentItems = items
                onContentAvailable(true)
                Log.d("IslamicContent", "Loaded ${items.size} items for džemat: $dzamijaId")
            } else {
                contentItems = emptyList()
                onContentAvailable(false)
            }
        }
    }

    LaunchedEffect(contentItems, currentIndex) {
        val currentItem = contentItems.getOrNull(currentIndex) ?: return@LaunchedEffect
        delay(currentItem.duration * 1000L)
        currentIndex = (currentIndex + 1) % contentItems.size
    }

    if (contentItems.isEmpty()) return

    val currentContent = contentItems.getOrNull(currentIndex) ?: return
    Column(modifier = modifier.background(Color(0xFF1B4332)), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (currentContent.imageUrl.isNotEmpty()) {
                val displayUrl = currentContent.imageUrl.replace("https://", "http://")
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = displayUrl,
                        contentDescription = "Slika obavještenja",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        onLoading = { Log.d("ImageLoad", "Loading: $displayUrl") },
                        onSuccess = { Log.d("ImageLoad", "✅ Success") },
                        onError = { error -> Log.e("ImageLoad", "❌ Error: ${error.result.throwable.message}") }
                    )
                }
            }
            Column {
                if (currentContent.arabicText.isNotEmpty()) {
                    ScrollableTextCard(currentContent.arabicText, 26.sp, 40.sp, Color(0xFF2D6A4F), Color.White)
                }
                if (currentContent.bosnianText.isNotEmpty()) {
                    ScrollableTextCard(currentContent.bosnianText, 22.sp, 32.sp, Color(0xFF40916C), Color(0xFFD8F3DC))
                }
            }
        }
    }
}

@Composable
fun ScrollableTextCard(text: String, fontSize: TextUnit, lineHeight: TextUnit, containerColor: Color, textColor: Color) {
    val scrollState = rememberScrollState()
    Card(modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Box(modifier = Modifier.padding(24.dp).verticalScroll(scrollState)) {
            Text(text = text, fontSize = fontSize, lineHeight = lineHeight, textAlign = TextAlign.Center, color = textColor, modifier = Modifier.fillMaxWidth())
        }
    }
}

fun getCurrentTime(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

fun getCurrentDateBosnian(): String {
    val calendar = Calendar.getInstance()
    val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Ponedjeljak"; Calendar.TUESDAY -> "Utorak"
        Calendar.WEDNESDAY -> "Srijeda"; Calendar.THURSDAY -> "Četvrtak"
        Calendar.FRIDAY -> "Petak"; Calendar.SATURDAY -> "Subota"
        Calendar.SUNDAY -> "Nedjelja"; else -> ""
    }
    val monthName = when (calendar.get(Calendar.MONTH)) {
        Calendar.JANUARY -> "Januar"; Calendar.FEBRUARY -> "Februar"
        Calendar.MARCH -> "Mart"; Calendar.APRIL -> "April"
        Calendar.MAY -> "Maj"; Calendar.JUNE -> "Juni"
        Calendar.JULY -> "Juli"; Calendar.AUGUST -> "August"
        Calendar.SEPTEMBER -> "Septembar"; Calendar.OCTOBER -> "Oktobar"
        Calendar.NOVEMBER -> "Novembar"; Calendar.DECEMBER -> "Decembar"
        else -> ""
    }
    return "${dayOfWeek}, ${calendar.get(Calendar.DAY_OF_MONTH)}. $monthName ${calendar.get(Calendar.YEAR)}."
}

fun getTimeInMinutes(time: String): Int {
    val parts = time.split(":").map { it.toInt() }
    return parts[0] * 60 + parts[1]
}

fun getCurrentTimeInMinutes(): Int {
    val now = Calendar.getInstance()
    return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
}

data class PrayerTime(val arabicName: String, val bosnianName: String, val time: String)

data class IslamicContent(
    val arabicText: String, val bosnianText: String,
    val reference: String, val isHadith: Boolean, val duration: Long = 15
)