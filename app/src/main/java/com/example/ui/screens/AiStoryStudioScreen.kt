package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Moment
import com.example.data.model.StoryChapter
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStoryStudioScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(1) } // 0: Capture, 1: Highlights & EDL, 2: Story & Narration
    val tabTitles = listOf("Capture & Mark", "Highlights & EDL", "Story & Voice")

    // Studio Settings
    var rideTitle by remember { mutableStateOf("Monsoon & Mountain Passes") }
    var locationName by remember { mutableStateOf("Varandha Ghat & Sahyadri Ridge") }
    var selectedLanguage by remember { mutableStateOf("English (Epic)") }
    var targetDurationSec by remember { mutableStateOf(60) }
    var selectedAspect by remember { mutableStateOf("16:9 Landscape") }

    // Capture Simulation State
    var isSimulatingRecording by remember { mutableStateOf(false) }
    var recordedSeconds by remember { mutableStateOf(84) }
    val markedBookmarks = remember { mutableStateListOf(14, 38, 59, 78) }

    // Moments List (scored from footage & bookmarks)
    val moments = remember {
        mutableStateListOf(
            Moment(
                startSec = 10.0,
                peakSec = 14.5,
                endSec = 22.0,
                label = "Varandha Waterfall Hairpin Drift",
                description = "High lateral lean angle through misty waterfall crossing with wet asphalt grip.",
                score = 0.94,
                tags = listOf("hairpin", "waterfall", "apex-lean"),
                speedKmh = 48,
                altitudeM = 1420,
                hasFaceOrPlateAlert = false,
                isSelected = true
            ),
            Moment(
                startSec = 32.0,
                peakSec = 38.0,
                endSec = 46.0,
                label = "Ridge Cloudburst Acceleration",
                description = "Fog clearing on cliff edge straightaway, golden-hour illumination breaking through mist.",
                score = 0.89,
                tags = listOf("cloudburst", "golden-hour", "ridge"),
                speedKmh = 72,
                altitudeM = 1580,
                hasFaceOrPlateAlert = false,
                isSelected = true
            ),
            Moment(
                startSec = 52.0,
                peakSec = 59.2,
                endSec = 67.0,
                label = "Gorge Canyon Overlook",
                description = "Pan across 1,800 ft drop into Konkan valley with mountain stream cascade.",
                score = 0.96,
                tags = listOf("scenic", "canyon", "panorama"),
                speedKmh = 36,
                altitudeM = 1640,
                hasFaceOrPlateAlert = true, // License plate / privacy tag
                isSelected = true
            ),
            Moment(
                startSec = 72.0,
                peakSec = 78.0,
                endSec = 84.0,
                label = "High Pass Summit Checkpoint",
                description = "Reaching the ancient stone archway milestone, steam rising from the dual-sport exhaust.",
                score = 0.91,
                tags = listOf("summit", "stone-pass", "victory"),
                speedKmh = 54,
                altitudeM = 1710,
                hasFaceOrPlateAlert = false,
                isSelected = true
            ),
            Moment(
                startSec = 92.0,
                peakSec = 98.0,
                endSec = 105.0,
                label = "Chai Tapri Pitstop Gathering",
                description = "Hot ginger tea brewing beside rain-soaked motorcycles with fellow riders.",
                score = 0.72,
                tags = listOf("chai", "brotherhood", "warmth"),
                speedKmh = 0,
                altitudeM = 1680,
                hasFaceOrPlateAlert = true, // Face detected
                isSelected = false
            )
        )
    }

    // Story Result Data
    val selectedMoments = moments.filter { it.isSelected }
    val totalReelSeconds = selectedMoments.sumOf { it.durationSec }.toInt()

    val storyDiary = remember(rideTitle, locationName, selectedLanguage) {
        when {
            selectedLanguage.contains("Hindi") ->
                "सह्याद्रि की धुंध भरी वादियों में हमारी मोटरसाइकिलों ने बादलों को चीरते हुए रास्ता बनाया। वरांधा घाट के हर एक तीखे मोड़ पर गिरते हुए झरनों का पानी और भीगी सड़कों पर टायरों की मजबूत पकड़ ने इस सफर को यादगार बना दिया। जब इंसान और मशीन एक हो जाते हैं, तब ऐसी ही कहानियाँ बनती हैं।"
            selectedLanguage.contains("Marathi") ->
                "सह्याद्रीच्या दर्‍याखोऱ्यांतून आणि धो-धो कोसळणाऱ्या जलप्रपातांच्या साक्षीने वरंधा घाटातील ही चित्तथरारक मोहीम! धुक्याची चादर आणि घाटाची वळणे पार करताना प्रत्येक सेकंदाला नव्या साहसाचा अनुभव आला. दोन चाकांवरची ही थरारक मुशाफिरी आयुष्यभर लक्षात राहील."
            else ->
                "Carving through the monsoon-drenched ridges of Varandha Ghat. The road disappears into heavy Sahyadri mist as waterfalls crash beside the asphalt. Pushing single-cylinder dual-sports through switchbacks, testing wet traction at 45° lean angles, and reaching the stone arch summit at 1,710m. This is what adventure riding is made of."
        }
    }

    val socialCaption = remember(rideTitle, locationName, selectedLanguage) {
        when {
            selectedLanguage.contains("Hindi") ->
                "🏔️ बादलों के पार, सह्याद्रि के तीखे मोड़ों पर! वरांधा घाट की भीगी सड़कों और तूफानी हवाओं के बीच एक यादगार राइड। 🏍️💨 #Adventurer #RideStory #WesternGhats #Himalayan450"
            selectedLanguage.contains("Marathi") ->
                "🌧️ सह्याद्रीची वळणे आणि वरंधा घाटाचा धुंवाधार पाऊस! दोन चाकांवरचा थरार आणि सोबतीला रायडर्सचा जथा। 🏍️✨ #VarandhaGhat #Adventurer #RideStory"
            else ->
                "🌧️ Where the road meets the mist: Conquering the switchbacks and cascades of Varandha Ghat. $totalReelSeconds seconds of pure high-pass adrenaline! 🏍️💨 #Adventurer #RideStory #SahyadriNomads #Himalayan450"
        }
    }

    val storyChapters = remember(selectedMoments) {
        var runningTime = 2.5 // Title card start
        selectedMoments.map { m ->
            val chap = StoryChapter(
                timeLabel = String.format("%02d:%04.1f", (runningTime / 60).toInt(), runningTime % 60),
                title = m.label,
                speedKmh = m.speedKmh,
                altitudeM = m.altitudeM
            )
            runningTime += m.durationSec
            chap
        }
    }

    // Text To Speech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = if (selectedLanguage.contains("Hindi")) Locale("hi", "IN") else Locale.ENGLISH
                tts = textToSpeech
            }
        }
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("RideStory AI Studio", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFC084FC).copy(alpha = 0.2f)) {
                                Text("V2 PRO", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC084FC), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text("Footage ➔ Scored Moments ➔ Reel Plan ➔ Narration", style = MaterialTheme.typography.labelSmall, color = AmberGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F17))
            )
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Header (Capture, Highlights, Story)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF131B2A),
                contentColor = AdventureOrange,
                divider = { Divider(color = Color(0xFF334155)) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == index) AdventureOrange else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    // TAB 0: Capture & Mark Moments
                    0 -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Camera & Trail Viewfinder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSimulatingRecording) DangerRed else Color(0xFF1E293B)
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(shape = CircleShape, color = if (isSimulatingRecording) Color.White else DangerRed, modifier = Modifier.size(8.dp)) {}
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    if (isSimulatingRecording) "REC 01:24" else "STANDBY",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Viewfinder Frame
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Videocam, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(48.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("1080p 60FPS • Gyro Steady • Audio Active", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                            Text("Route: $locationName", style = MaterialTheme.typography.labelSmall, color = AmberGold)
                                        }

                                        // Mark Moment Floating Button in Viewfinder
                                        Button(
                                            onClick = {
                                                val mark = recordedSeconds
                                                markedBookmarks.add(mark)
                                                Toast.makeText(context, "Marked moment at ${mark}s! (${markedBookmarks.size} bookmarks)", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(12.dp)
                                                .testTag("mark_moment_button")
                                        ) {
                                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("MARK MOMENT (M)", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                isSimulatingRecording = !isSimulatingRecording
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSimulatingRecording) DangerRed else AdventureOrange
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(if (isSimulatingRecording) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isSimulatingRecording) "Stop Capture" else "Start Capture", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                activeTab = 1
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Proceed to Highlights", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("Marked Trail Bookmarks (${markedBookmarks.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                markedBookmarks.forEach { mark ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${mark}s Peak", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: Highlights & EDL Timeline
                    1 -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Reel Production Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                    // Target Duration
                                    Text("Target Reel Duration:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(30, 60, 90, 120, 180).forEach { dur ->
                                            val label = if (dur < 60) "${dur}s" else "${dur / 60}m"
                                            FilterChip(
                                                selected = targetDurationSec == dur,
                                                onClick = { targetDurationSec = dur },
                                                label = { Text(label) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AdventureOrange,
                                                    selectedLabelColor = Color.Black,
                                                    containerColor = Color(0xFF1E293B),
                                                    labelColor = Color.White
                                                )
                                            )
                                        }
                                    }

                                    // Aspect Ratio
                                    Text("Aspect Format:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("16:9 Landscape", "9:16 Vertical Reel").forEach { asp ->
                                            FilterChip(
                                                selected = selectedAspect == asp,
                                                onClick = { selectedAspect = asp },
                                                label = { Text(asp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AmberGold,
                                                    selectedLabelColor = Color.Black,
                                                    containerColor = Color(0xFF1E293B),
                                                    labelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Reel Duration Summary Bar
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("REEL TIMELINE TALLY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                        Text(
                                            text = "$totalReelSeconds s / $targetDurationSec s (${selectedMoments.size} Clips Selected)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = HighAltitudeCyan
                                        )
                                    }

                                    Button(
                                        onClick = { activeTab = 2 },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Generate Story ➔", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Scored Moments List
                        item {
                            Text("AI Scored Video Moments (Ranked)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        items(moments) { moment ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (moment.isSelected) AdventureOrange else Color(0xFF334155)
                                    )
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Checkbox(
                                                checked = moment.isSelected,
                                                onCheckedChange = { checked ->
                                                    val idx = moments.indexOf(moment)
                                                    if (idx != -1) moments[idx] = moment.copy(isSelected = checked)
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = AdventureOrange)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(moment.label, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                                                Text(
                                                    "${String.format("%.1f", moment.startSec)}s - ${String.format("%.1f", moment.endSec)}s (${moment.durationSec.toInt()}s cut) • Peak: ${moment.speedKmh} km/h",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }
                                        }

                                        // AI Score Badge
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AmberGold.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${(moment.score * 100).toInt()}% SCORE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AmberGold,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(moment.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))

                                    // Privacy alert if applicable
                                    if (moment.hasFaceOrPlateAlert) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = DangerRed.copy(alpha = 0.15f)) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Shield, contentDescription = null, tint = DangerRed, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Privacy Flag: Plate / Face Detected", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: Story, Chapters & Voice Narration
                    2 -> {
                        item {
                            // Language switcher
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Narration Language & Voice:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("English (Epic)", "Hindi (हिन्दी)", "Marathi (मराठी)").forEach { l ->
                                            FilterChip(
                                                selected = selectedLanguage == l,
                                                onClick = {
                                                    selectedLanguage = l
                                                    if (tts != null) {
                                                        tts?.language = if (l.contains("Hindi")) Locale("hi", "IN") else Locale.ENGLISH
                                                    }
                                                },
                                                label = { Text(l) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AmberGold,
                                                    selectedLabelColor = Color.Black,
                                                    containerColor = Color(0xFF1E293B),
                                                    labelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Social Caption Card with Copy
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF8B5CF6))
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Social Caption & Hashtags", fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Story Caption", socialCaption))
                                                Toast.makeText(context, "Caption copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFC084FC))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = socialCaption, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE2E8F0))
                                }
                            }
                        }

                        // Travel Diary Article
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Article, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Travel Diary & Voice Narration", fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        // Text-To-Speech Listen Button
                                        Button(
                                            onClick = {
                                                if (isSpeaking) {
                                                    tts?.stop()
                                                    isSpeaking = false
                                                } else {
                                                    tts?.speak(storyDiary, TextToSpeech.QUEUE_FLUSH, null, "story_audio")
                                                    isSpeaking = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSpeaking) DangerRed else AdventureOrange
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isSpeaking) "Stop Audio" else "Listen (TTS)", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(text = storyDiary, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCBD5E1), lineHeight = 22.sp)
                                }
                            }
                        }

                        // Reel Chapters Timeline
                        item {
                            Text("Reel Chapters (${storyChapters.size} Milestones)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        items(storyChapters) { chap ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF131B2A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = AmberGold.copy(alpha = 0.2f)) {
                                        Text(chap.timeLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AmberGold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(chap.title, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                        Text("Speed: ${chap.speedKmh} km/h • Elevation: ${chap.altitudeM}m", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }

                        // Bottom Actions: Share to Community Feed & Export EDL
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.createPost(
                                            caption = socialCaption,
                                            imageUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=1000&q=80"
                                        )
                                        Toast.makeText(context, "Story published to community feed!", Toast.LENGTH_SHORT).show()
                                        viewModel.navigateTo(com.example.viewmodel.Screen.FEED)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Post to Feed", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val edlJson = """
                                            {
                                              "title": "$rideTitle",
                                              "aspect": "$selectedAspect",
                                              "targetSeconds": $targetDurationSec,
                                              "moments": [
                                                ${selectedMoments.joinToString(",") { 
                                                    """{"label": "${it.label}", "start": ${it.startSec}, "end": ${it.endSec}, "score": ${it.score}}""" 
                                                }}
                                              ]
                                            }
                                        """.trimIndent()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("EDL JSON", edlJson))
                                        Toast.makeText(context, "EDL JSON exported and copied to clipboard!", Toast.LENGTH_LONG).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = HighAltitudeCyan)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export EDL JSON", color = HighAltitudeCyan)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
