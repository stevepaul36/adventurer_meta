package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.HighlightClip
import com.example.data.ai.RideStoryAiService
import kotlinx.coroutines.launch
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStoryStudioScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    var selectedRegion by remember { mutableStateOf("Leh-Ladakh High Passes") }
    var targetLength by remember { mutableStateOf("60s Reel") }
    var narrationLang by remember { mutableStateOf("English (Adventure Epic)") }

    var highlightClips by remember {
        mutableStateOf(
            listOf(
                HighlightClip("00:14 - 00:22", "Khardung La Summit Ascent", 48, 5359, "Hairpin Carve", true),
                HighlightClip("01:05 - 01:18", "Pangong Tso Turquoise Shoreline", 64, 4270, "High Speed Drift", true),
                HighlightClip("02:40 - 02:52", "Nubra Sand Dunes Golden Sunset", 52, 3048, "Scenic Panorama", true),
                HighlightClip("04:12 - 04:26", "Baralacha Glacial Water Crossing", 24, 4890, "River Splashdown", true),
                HighlightClip("06:30 - 06:44", "Umling La 19,024 ft Finish Flag", 42, 5798, "Summit Victory", true)
            )
        )
    }

    val fallbackCaption = remember(selectedRegion, narrationLang) {
        when {
            narrationLang.contains("Hindi") ->
                "🏔️ लद्दाख की बर्फीली हवाओं और ऊँचाई पर हमारी बाइकों की गूंज! हर मोड़ पर एक नई दास्तान। एडवेंचर कभी रुकता नहीं! 🏍️💨 #LehLadakh #AdventurerRider"
            narrationLang.contains("Marathi") ->
                "सह्याद्री ते हिमालयाचे उत्तुंग शिखर! दोन चाकांवरची थरारक सफर आणि प्रत्येक वळणावर नवा अनुभव. 🏍️✨ #HimalayanOdyssey #Adventurer"
            else ->
                "🏔️ The road unfolds one turn at a time. Big skies, mountain air, and the moments that make every ride a story. 🏍️💨 #Adventurer #RideStory"
        }
    }
    var generatedCaption by remember(selectedRegion, narrationLang) { mutableStateOf(fallbackCaption) }
    var isGenerating by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Ride Story Studio", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Highlight Timeline & Multilingual Narration", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC084FC))
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuration Controls
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
                        Text("Story Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                        // Target Region
                        Text("Expedition Route:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Leh-Ladakh High Passes", "Spiti Valley Loop", "Western Ghats Monsoon").forEach { r ->
                                FilterChip(
                                    selected = selectedRegion == r,
                                    onClick = { selectedRegion = r },
                                    label = { Text(r) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AdventureOrange,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        Text("Target Length:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("15s Reel", "30s Reel", "60s Reel").forEach { length ->
                                FilterChip(
                                    selected = targetLength == length,
                                    onClick = { targetLength = length },
                                    label = { Text(length) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HighAltitudeCyan,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Narration Language
                        Text("Narration Voice & Language:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("English (Adventure Epic)", "Hindi (हिन्दी जोश)", "Marathi (मराठी शौर्य)").forEach { l ->
                                FilterChip(
                                    selected = narrationLang == l,
                                    onClick = { narrationLang = l },
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

            // AI Generated Travel Caption with Copy
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_caption_card"),
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
                                Text("AI Generated Social Caption", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Ride Caption", generatedCaption))
                                    Toast.makeText(context, "Caption copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFC084FC))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = generatedCaption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                        if (aiError != null) {
                            Text(
                                text = aiError!!,
                                color = Color(0xFFFCA5A5),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (!isGenerating) {
                                        isGenerating = true
                                        aiError = null
                                        coroutineScope.launch {
                                            try {
                                                val selected = highlightClips.filter { it.isSelected }
                                                if (selected.isEmpty()) {
                                                    aiError = "Select at least one highlight before generating a story."
                                                } else {
                                                    generatedCaption = RideStoryAiService.generateCaption(
                                                        region = selectedRegion,
                                                        targetLength = targetLength,
                                                        narrationLanguage = narrationLang,
                                                        clips = selected.map {
                                                            RideStoryAiService.ClipContext(
                                                                timeRange = it.timestamp,
                                                                title = it.title,
                                                                tag = it.tag,
                                                                speedKmh = it.speedKmh,
                                                                altitudeM = it.altitudeM
                                                            )
                                                        }
                                                    )
                                                }
                                            } catch (error: Exception) {
                                                aiError = "AI generation failed. Check Firebase AI Logic setup and App Check, then try again. Your current caption is preserved."
                                            } finally {
                                                isGenerating = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isGenerating,
                                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating…", color = Color.Black)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generate with AI", color = Color.Black)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.createPost(generatedCaption, null)
                                    Toast.makeText(context, "Ride story shared to your feed.", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            // Candidate Highlight Clips Timeline (EDL)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Highlight Reel Timeline (${highlightClips.count { it.isSelected }} Clips Selected)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    AssistChip(
                        onClick = {
                            val selectedClips = highlightClips.filter { it.isSelected }
                            if (selectedClips.isEmpty()) {
                                Toast.makeText(context, "Select at least one highlight to export.", Toast.LENGTH_SHORT).show()
                            } else {
                                val edlJson = JSONObject()
                                    .put("format", "adventurer-edl-v1")
                                    .put("region", selectedRegion)
                                    .put("targetLength", targetLength)
                                    .put("narrationLanguage", narrationLang)
                                    .put("clips", JSONArray().apply {
                                        selectedClips.forEach { clip ->
                                            put(
                                                JSONObject()
                                                    .put("timeRange", clip.timestamp)
                                                    .put("title", clip.title)
                                                    .put("tag", clip.tag)
                                                    .put("speedKmh", clip.speedKmh)
                                                    .put("altitudeM", clip.altitudeM)
                                            )
                                        }
                                    })
                                    .toString(2)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Adventurer Ride Story EDL", edlJson))
                                Toast.makeText(
                                    context,
                                    "Copied ${selectedClips.size} highlight${if (selectedClips.size == 1) "" else "s"} as EDL JSON.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        label = { Text("Export EDL JSON", color = HighAltitudeCyan) },
                        leadingIcon = {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = HighAltitudeCyan, modifier = Modifier.size(14.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B))
                    )
                }
            }

            items(highlightClips) { clip ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131B2A),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (clip.isSelected) AdventureOrange else Color(0xFF334155)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = clip.isSelected,
                            onCheckedChange = { checked ->
                                highlightClips = highlightClips.map {
                                    if (it.timestamp == clip.timestamp) it.copy(isSelected = checked) else it
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = AdventureOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(clip.title, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B)) {
                                    Text(clip.tag, style = MaterialTheme.typography.labelSmall, color = AmberGold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "⏱️ ${clip.timestamp} • Speed: ${clip.speedKmh} km/h • Alt: ${clip.altitudeM}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}
