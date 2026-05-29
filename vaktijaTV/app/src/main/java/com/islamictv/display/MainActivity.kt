package com.islamictv.display

import android.content.res.Resources
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
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.clickable

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

// ---------------------------------------------------------------------------
// Detekcija rezolucije
// ---------------------------------------------------------------------------

enum class ScreenClass { TV_4K, TV_1080P, TV_720P }

@Composable
fun rememberScreenClass(): ScreenClass {
    val realWidthPx = Resources.getSystem().displayMetrics.widthPixels

    Log.d("ScreenDebug", "REAL widthPx=$realWidthPx")

    return remember(realWidthPx) {
        when {
            realWidthPx >= 3000 -> ScreenClass.TV_4K
            realWidthPx >= 1800 -> ScreenClass.TV_1080P
            else -> ScreenClass.TV_720P
        }
    }
}

// ---------------------------------------------------------------------------
// Dimenzije po rezoluciji
// ---------------------------------------------------------------------------

data class TVDimensions(
    val arabicTitleSp: Int,
    val arabicTitleFullSp: Int,
    val cityTitleSp: Int,
    val cityTitleFullSp: Int,
    val clockSp: Int,
    val clockFullSp: Int,
    val dateSp: Int,
    val dateFullSp: Int,
    val prayerArabicSp: Int,
    val prayerArabicFullSp: Int,
    val prayerBosnianSp: Int,
    val prayerBosnianFullSp: Int,
    val prayerTimeSp: Int,
    val prayerTimeFullSp: Int,
    val countdownSp: Int,
    val countdownFullSp: Int,
    val loadingTextSp: Int,
    val setupTitleSp: Int,
    val setupBodySp: Int,
    val panelPaddingDp: Int,
    val cardSpacingDp: Int,
    val cardPaddingHDp: Int,
    val cardPaddingVDp: Int,
    val headerSpacerDp: Int,
    val scrollCardMaxHeightDp: Int,
    val arabicScrollSp: Int,
    val arabicScrollLineSp: Int,
    val bosnianScrollSp: Int,
    val bosnianScrollLineSp: Int
)

fun dimensionsFor(screen: ScreenClass): TVDimensions = when (screen) {
    ScreenClass.TV_4K -> TVDimensions(
        arabicTitleSp = 36,arabicTitleFullSp = 40, cityTitleSp = 28,cityTitleFullSp = 32, clockSp = 60,clockFullSp = 70, dateSp = 25,dateFullSp = 30,
        prayerArabicSp = 26,prayerArabicFullSp = 34, prayerBosnianSp = 24,prayerBosnianFullSp = 30,
        prayerTimeSp = 28, prayerTimeFullSp = 36,
        countdownSp = 18, countdownFullSp = 25,
        loadingTextSp = 24, setupTitleSp = 28, setupBodySp = 18,
        panelPaddingDp = 24, cardSpacingDp = 12,
        cardPaddingHDp = 16, cardPaddingVDp = 10, headerSpacerDp = 24,
        scrollCardMaxHeightDp = 260,
        arabicScrollSp = 26, arabicScrollLineSp = 40,
        bosnianScrollSp = 22, bosnianScrollLineSp = 32
    )
    ScreenClass.TV_1080P -> TVDimensions(
        arabicTitleSp = 20,arabicTitleFullSp = 25, cityTitleSp = 24,cityTitleFullSp = 27, clockSp = 48,clockFullSp = 58, dateSp = 20,dateFullSp = 25,
        prayerArabicSp = 18,prayerArabicFullSp = 34, prayerBosnianSp = 16,prayerBosnianFullSp = 22,
        prayerTimeSp = 22, prayerTimeFullSp = 30,
        countdownSp = 13, countdownFullSp = 20,
        loadingTextSp = 17, setupTitleSp = 21, setupBodySp = 15,
        panelPaddingDp = 15, cardSpacingDp = 8,
        cardPaddingHDp = 11, cardPaddingVDp = 7, headerSpacerDp = 15,
        scrollCardMaxHeightDp = 160,
        arabicScrollSp = 17, arabicScrollLineSp = 27,
        bosnianScrollSp = 15, bosnianScrollLineSp = 23
    )
    ScreenClass.TV_720P -> TVDimensions(
        arabicTitleSp = 22,arabicTitleFullSp = 25, cityTitleSp = 24,cityTitleFullSp = 27, clockSp = 48,clockFullSp = 68, dateSp = 22,dateFullSp = 25,
        prayerArabicSp = 18,prayerArabicFullSp = 34, prayerBosnianSp = 16,prayerBosnianFullSp = 22,
        prayerTimeSp = 21, prayerTimeFullSp = 30,
        countdownSp = 13, countdownFullSp = 20,
        loadingTextSp = 17, setupTitleSp = 21, setupBodySp = 15,
        panelPaddingDp = 15, cardSpacingDp = 8,
        cardPaddingHDp = 11, cardPaddingVDp = 7, headerSpacerDp = 15,
        scrollCardMaxHeightDp = 160,
        arabicScrollSp = 17, arabicScrollLineSp = 27,
        bosnianScrollSp = 15, bosnianScrollLineSp = 23
    )
}

val LocalTVDimensions = compositionLocalOf { dimensionsFor(ScreenClass.TV_4K) }

// ---------------------------------------------------------------------------
// Root composable
// ---------------------------------------------------------------------------

@Composable
fun TVApp() {
    val context = LocalContext.current
    val screenClass = rememberScreenClass()
    val dims = remember(screenClass) { dimensionsFor(screenClass) }

    var isConfigured by remember { mutableStateOf(TVDzamatConfig.isConfigured(context)) }

    Log.d("ScreenClass", "Detected: $screenClass (clockSp=${dims.clockSp})")

    CompositionLocalProvider(LocalTVDimensions provides dims) {
        if (!isConfigured) {
            TVSetupScreen(onConfigured = { isConfigured = true })
        } else {
            SplitScreenDisplay()
        }
    }
}

// ---------------------------------------------------------------------------
// Setup screen
// ---------------------------------------------------------------------------

val GRADOVI_BIH = listOf(
    "Banovići" to 0,
    "Banja Luka" to 1,
    "Bihać" to 2,
    "Bijeljina" to 3,
    "Bileća" to 4,
    "Bosanski Brod" to 5,
    "Bosanska Dubica" to 6,
    "Bosanska Gradiška" to 7,
    "Bosansko Grahovo" to 8,
    "Bosanska Krupa" to 9,
    "Bosanski Novi" to 10,
    "Bosanski Petrovac" to 11,
    "Bosanski Šamac" to 12,
    "Bratunac" to 13,
    "Brčko" to 14,
    "Breza" to 15,
    "Bugojno" to 16,
    "Busovača" to 17,
    "Bužim" to 18,
    "Cazin" to 19,
    "Čajniče" to 20,
    "Čapljina" to 21,
    "Čelić" to 22,
    "Čelinac" to 23,
    "Čitluk" to 24,
    "Derventa" to 25,
    "Doboj" to 26,
    "Donji Vakuf" to 27,
    "Drvar" to 28,
    "Foča" to 29,
    "Fojnica" to 30,
    "Gacko" to 31,
    "Glamoč" to 32,
    "Goražde" to 33,
    "Gornji Vakuf" to 34,
    "Gračanica" to 35,
    "Gradačac" to 36,
    "Grude" to 37,
    "Hadžići" to 38,
    "Han-Pijesak" to 39,
    "Hlivno" to 40,
    "Ilijaš" to 41,
    "Jablanica" to 42,
    "Jajce" to 43,
    "Kakanj" to 44,
    "Kalesija" to 45,
    "Kalinovik" to 46,
    "Kiseljak" to 47,
    "Kladanj" to 48,
    "Ključ" to 49,
    "Konjic" to 50,
    "Kotor-Varoš" to 51,
    "Kreševo" to 52,
    "Kupres" to 53,
    "Laktaši" to 54,
    "Lopare" to 55,
    "Lukavac" to 56,
    "Ljubinje" to 57,
    "Ljubuški" to 58,
    "Maglaj" to 59,
    "Modriča" to 60,
    "Mostar" to 61,
    "Mrkonjić-Grad" to 62,
    "Neum" to 63,
    "Nevesinje" to 64,
    "Novi Travnik" to 65,
    "Odžak" to 66,
    "Olovo" to 67,
    "Orašje" to 68,
    "Pale" to 69,
    "Posušje" to 70,
    "Prijedor" to 71,
    "Prnjavor" to 72,
    "Prozor" to 73,
    "Rogatica" to 74,
    "Rudo" to 75,
    "Sanski Most" to 76,
    "Sarajevo" to 77,
    "Skender-Vakuf" to 78,
    "Sokolac" to 79,
    "Srbac" to 80,
    "Srebrenica" to 81,
    "Srebrenik" to 82,
    "Stolac" to 83,
    "Šekovići" to 84,
    "Šipovo" to 85,
    "Široki Brijeg" to 86,
    "Teslić" to 87,
    "Tešanj" to 88,
    "Tomislav-Grad" to 89,
    "Travnik" to 90,
    "Trebinje" to 91,
    "Trnovo" to 92,
    "Tuzla" to 93,
    "Ugljevik" to 94,
    "Vareš" to 95,
    "Velika Kladuša" to 96,
    "Visoko" to 97,
    "Višegrad" to 98,
    "Vitez" to 99,
    "Vlasenica" to 100,
    "Zavidovići" to 101,
    "Zenica" to 102,
    "Zvornik" to 103,
    "Žepa" to 104,
    "Žepče" to 105,
    "Živinice" to 106,
    "Bijelo Polje" to 107,
    "Gusinje" to 108,
    "Nova Varoš" to 109,
    "Novi Pazar" to 110,
    "Plav" to 111,
    "Pljevlja" to 112,
    "Priboj" to 113,
    "Prijepolje" to 114,
    "Rožaje" to 115,
    "Sjenica" to 116,
    "Tutin" to 117
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TVSetupScreen(onConfigured: () -> Unit) {
    val context = LocalContext.current
    val dims = LocalTVDimensions.current
    var dzamijaId by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedGrad by remember { mutableStateOf(GRADOVI_BIH.first { it.second == 61 }) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1B4332)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.5f).padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D6A4F))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Postavljanje TV ekrana",
                    fontSize = dims.setupTitleSp.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Unesite ID vašeg džemata.\nOvo se radi samo jednom.",
                    fontSize = dims.setupBodySp.sp,
                    color = Color(0xFFB7E4C7),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dzamijaId,
                    onValueChange = { dzamijaId = it; error = null },
                    label = { Text("ID džemata", color = Color(0xFFB7E4C7)) },
                    placeholder = { Text("npr. dzemat123", color = Color(0xFF74C69D)) },
                    supportingText = {
                        Text(
                            "ID dobijate od administratora sistema pri postavljanju.",
                            color = Color(0xFF74C69D), fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF74C69D), unfocusedBorderColor = Color(0xFF40916C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )


                    // Umjesto OutlinedTextField, koristi Button
                    Button(
                        onClick = { dropdownExpanded = !dropdownExpanded },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF40916C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Grad: ${selectedGrad.first}",
                            color = Color.White,
                            fontSize = dims.setupBodySp.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    if (dropdownExpanded) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D6A4F))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                GRADOVI_BIH.forEach { grad ->
                                    Button(
                                        onClick = { selectedGrad = grad; dropdownExpanded = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (grad == selectedGrad) Color(0xFF40916C) else Color.Transparent
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = grad.first,
                                            color = Color.White,
                                            fontSize = dims.setupBodySp.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                        }
                    }

                if (error != null) Text(error!!, color = Color(0xFFFFE66D), fontSize = 14.sp)
                Button(
                    onClick = {
                        if (dzamijaId.isBlank()) error = "ID džemata ne može biti prazan"
                        else {
                            TVDzamatConfig.saveDzamijaId(context, dzamijaId)
                            TVDzamatConfig.saveLocation(context, selectedGrad.second, selectedGrad.first)
                            onConfigured()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF40916C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Potvrdi", fontSize = dims.setupBodySp.sp, color = Color.White)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Split screen
// ---------------------------------------------------------------------------

@Composable
fun SplitScreenDisplay() {
    var hasContent by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF1B4332))) {
        PrayerTimesPanel(
            modifier = Modifier.weight(if (hasContent) 1f else 2f).fillMaxHeight(),
            isFullScreen = !hasContent
        )
        if (hasContent) {
            IslamicContentPanel(
                modifier = Modifier.weight(1f).fillMaxHeight(),
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

// ---------------------------------------------------------------------------
// Prayer times panel
// ---------------------------------------------------------------------------

@Composable
fun PrayerTimesPanel(modifier: Modifier = Modifier, isFullScreen: Boolean = false) {
    val context = LocalContext.current
    val dims = LocalTVDimensions.current
    val locationId = remember { TVDzamatConfig.getLocationId(context) }
    val cityName = remember { TVDzamatConfig.getCityName(context) }
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
                } else { currentMinutes < nextMinutes }
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
                    val newTimes = api.fetchPrayerTimes(locationId)
                    if (newTimes != null) {
                        prayerTimes = newTimes
                        lastSuccessfulFetch = System.currentTimeMillis()
                        Log.d("PrayerTimes", "✅ API SUCCESS!! Times updated")
                    }
                } catch (e: Exception) { Log.e("PrayerTimes", "❌ Exception: ${e.message}") }
            }
            delay(30_000L)
        }
    }

    LaunchedEffect(Unit) {
        while (true) { delay(1000); currentTime = getCurrentTime(); currentDate = getCurrentDateBosnian() }
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

    Column(modifier = modifier.background(Color(0xFF2D6A4F)).padding(dims.panelPaddingDp.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("مواقيت الصلاة", fontSize = if (isFullScreen) dims.arabicTitleFullSp.sp else dims.arabicTitleSp.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Vaktija - $cityName", fontSize =if (isFullScreen) dims.cityTitleFullSp.sp else  dims.cityTitleSp.sp, fontWeight = FontWeight.Medium, color = Color(0xFFB7E4C7))
            Text(currentTime, fontSize = if (isFullScreen) dims.clockFullSp.sp else dims.clockSp.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD8F3DC))
            Text(currentDate, fontSize = if (isFullScreen) dims.dateFullSp.sp else dims.dateSp.sp, color = Color(0xFFB7E4C7))
        }

        Spacer(modifier = Modifier.height(dims.headerSpacerDp.dp))

        if (prayerTimes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Učitavanje vaktije...", fontSize = dims.loadingTextSp.sp, color = Color(0xFFFFE66D))
            }
        } else if (isFullScreen) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(dims.cardSpacingDp.dp)
            ) {
                val rows = prayerTimes.chunked(2)
                rows.forEach { rowPrayers ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(dims.cardSpacingDp.dp)
                    ) {
                        rowPrayers.forEach { prayer ->
                            PrayerTimeRow(
                                prayer = prayer,
                                showCountdown = prayer == nextPrayer,
                                countdownText = countdownText,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                isFullScreen = true
                            )
                        }
                        if (rowPrayers.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(dims.cardSpacingDp.dp)
            ) {
                prayerTimes.forEach { prayer ->
                    PrayerTimeRow(
                        prayer = prayer,
                        showCountdown = prayer == nextPrayer,
                        countdownText = countdownText,
                        isFullScreen = false
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Prayer time row
// ---------------------------------------------------------------------------

@Composable
fun PrayerTimeRow(
    prayer: PrayerTime,
    showCountdown: Boolean,
    countdownText: String,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean
) {
    val dims = LocalTVDimensions.current
    val backgroundColor = if (showCountdown) Color(0xFF38A169) else Color(0xFF40916C)
    val borderColor = if (showCountdown) Color(0xFF276749) else Color.Transparent

    Card(
        modifier = modifier.fillMaxWidth().border(3.dp, borderColor, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight()
                .padding(horizontal = dims.cardPaddingHDp.dp, vertical = dims.cardPaddingVDp.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(prayer.arabicName, fontSize = if (isFullScreen) dims.prayerArabicFullSp.sp else dims.prayerArabicSp.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("  |  ", fontSize = if (isFullScreen) dims.prayerArabicFullSp.sp else  dims.prayerArabicSp.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD8F3DC))
                Text(prayer.bosnianName, fontSize = if (isFullScreen) dims.prayerBosnianFullSp.sp else  dims.prayerBosnianSp.sp, color = Color(0xFFD8F3DC))
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                Text(
                    prayer.time,
                    fontSize = if (isFullScreen) dims.prayerTimeFullSp.sp else dims.prayerTimeSp.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFE66D)
                )
                if (showCountdown) {
                    Text(
                        countdownText,
                        fontSize = if (isFullScreen) dims.countdownFullSp.sp else dims.countdownSp.sp,
                        color = Color(0xFFFFE66D)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Islamic content panel
// ---------------------------------------------------------------------------

@Composable
fun IslamicContentPanel(
    modifier: Modifier = Modifier,
    onContentAvailable: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val dims = LocalTVDimensions.current
    var currentIndex by remember { mutableStateOf(0) }
    var contentItems by remember { mutableStateOf<List<ContentItem>>(emptyList()) }

    LaunchedEffect(contentItems) { currentIndex = 0 }

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
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    ScrollableTextCard(
                        text = currentContent.arabicText,
                        fontSize = dims.arabicScrollSp.sp,
                        lineHeight = dims.arabicScrollLineSp.sp,
                        containerColor = Color(0xFF2D6A4F),
                        textColor = Color.White,
                        maxHeightDp = dims.scrollCardMaxHeightDp
                    )
                }
                if (currentContent.bosnianText.isNotEmpty()) {
                    ScrollableTextCard(
                        text = currentContent.bosnianText,
                        fontSize = dims.bosnianScrollSp.sp,
                        lineHeight = dims.bosnianScrollLineSp.sp,
                        containerColor = Color(0xFF40916C),
                        textColor = Color(0xFFD8F3DC),
                        maxHeightDp = dims.scrollCardMaxHeightDp
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Scrollable text card
// ---------------------------------------------------------------------------

@Composable
fun ScrollableTextCard(
    text: String,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    containerColor: Color,
    textColor: Color,
    maxHeightDp: Int = 260
) {
    val scrollState = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(max = maxHeightDp.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(modifier = Modifier.padding(24.dp).verticalScroll(scrollState)) {
            Text(
                text = text, fontSize = fontSize, lineHeight = lineHeight,
                textAlign = TextAlign.Center, color = textColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

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