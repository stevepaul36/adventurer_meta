package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.data.billing.AdventurerProBilling
import com.example.data.ai.RideStoryAiService
import com.example.data.ai.RideStoryVideoExporter
import java.io.File
import kotlinx.coroutines.launch
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStoryStudioScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val proBillingState by AdventurerProBilling.state.collectAsState()
    var selectedRegion by remember { mutableStateOf("Leh-Ladakh High Passes") }
    var targetLength by remember { mutableStateOf("60s Reel") }
    var narrationLang by remember { mutableStateOf("English (Adventure Epic)") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var analyzedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingEdlJson by remember { mutableStateOf("") }
    var pendingVideoFile by remember { mutableStateOf<File?>(null) }
    var isExportingReel by remember { mutableStateOf(false) }
    var reelExportError by remember { mutableStateOf<String?>(null) }
    val reelFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("video/mp4")
    ) { uri ->
        val file = pendingVideoFile
        if (uri != null && file != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    file.inputStream().use { input -> input.copyTo(output) }
                } ?: error("Could not open the selected destination.")
            }.onSuccess {
                Toast.makeText(context, "Ride highlight reel saved as MP4.", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(context, "Could not save the MP4. Try another location.", Toast.LENGTH_LONG).show()
            }.also {
                file.delete()
                pendingVideoFile = null
            }
        } else {
            file?.delete()
            pendingVideoFile = null
        }
    }
    val edlFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pendingEdlJson.toByteArray(Charsets.UTF_8))
                } ?: error("Could not open the selected destination.")
            }.onSuccess {
                Toast.makeText(context, "EDL JSON saved.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Could not save EDL JSON. Try another location.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Populate the timeline only with moments found in the user's selected video.
    var highlightClips by remember {
        mutableStateOf<List<HighlightClip>>(emptyList())
    }
    var generatedCaption by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var isAnalyzingHighlights by remember { mutableStateOf(false) }
    var highlightAnalysisError by remember { mutableStateOf<String?>(null) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedVideoUri = uri
        analyzedVideoUri = null
        highlightClips = emptyList()
        generatedCaption = ""
        aiError = null
        highlightAnalysisError = null
        reelExportError = null
    }

    // Settings are inputs to a caption. Clear output when the inputs change so the
    // UI never implies a previous caption was generated for a new configuration.
    LaunchedEffect(selectedRegion, targetLength, narrationLang) {
        generatedCaption = ""
        aiError = null
    }

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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("adventurer_pro_status"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2B)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (proBillingState.isPro) AmberGold else AdventureOrange.copy(alpha = 0.75f)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (proBillingState.isPro) Icons.Default.Verified else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (proBillingState.isPro) AmberGold else AdventureOrange
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (proBillingState.isPro) "Adventurer Pro is active" else "Unlock AI with Adventurer Pro",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (proBillingState.isPro) "AI highlight discovery and generated captions are unlocked."
                                else "AI highlight discovery and generated captions require a subscription.",
                                color = Color(0xFFCBD5E1),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        OutlinedButton(onClick = { viewModel.navigateTo(Screen.PRO_PAYWALL) }) {
                            Text(if (proBillingState.isPro) "Manage" else "View plans")
                        }
                    }
                }
            }

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

                        Text("Ride Footage (required for AI highlights)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(onClick = { videoPicker.launch("video/*") }) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (selectedVideoUri == null) "Choose video" else "Change video")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (selectedVideoUri == null) "No video selected" else "Video selected",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "For AI analysis, your selected video is uploaded temporarily to our private backend (14 MiB limit) and deleted after processing.",
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (!proBillingState.isPro) {
                                    viewModel.navigateTo(Screen.PRO_PAYWALL)
                                } else {
                                    val source = selectedVideoUri
                                    if (source == null) {
                                    highlightAnalysisError = "Choose a video before asking AI to find highlights."
                                } else if (!isAnalyzingHighlights) {
                                    isAnalyzingHighlights = true
                                    highlightAnalysisError = null
                                    coroutineScope.launch {
                                        try {
                                            val suggestions = RideStoryAiService.analyzeVideoForHighlights(
                                                context = context,
                                                videoUri = source,
                                                targetLength = targetLength,
                                                narrationLanguage = narrationLang
                                            )
                                            if (suggestions.isEmpty()) {
                                                highlightAnalysisError = "AI could not find reliable highlights. Try a different video."
                                            } else {
                                                analyzedVideoUri = source
                                                highlightClips = suggestions.map { suggestion ->
                                                    HighlightClip(
                                                        suggestion.timeRange,
                                                        suggestion.title,
                                                        0,
                                                        0,
                                                        suggestion.tag,
                                                        true
                                                    )
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "AI suggested ${suggestions.size} highlight clips.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        } catch (error: Exception) {
                                            highlightAnalysisError = error.message
                                                ?: "AI highlight analysis failed. Check Firebase configuration and try again."
                                        } finally {
                                            isAnalyzingHighlights = false
                                        }
                                    }
                                }
                                }
                            },
                            enabled = selectedVideoUri != null && !isAnalyzingHighlights,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
                        ) {
                            if (isAnalyzingHighlights) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing footage…", color = Color.Black)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Find Highlights", color = Color.Black)
                            }
                        }
                        if (highlightAnalysisError != null) {
                            Text(
                                text = highlightAnalysisError!!,
                                color = Color(0xFFFCA5A5),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (selectedVideoUri == null) {
                            Text(
                                "The initial timeline contains demo moments. Choose a real video and run AI Find Highlights before exporting a reel.",
                                color = Color(0xFFFBBF24),
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else if (analyzedVideoUri != selectedVideoUri) {
                            Text(
                                "Run AI Find Highlights for this video before rendering, so the timeline timestamps match your footage.",
                                color = Color(0xFFFBBF24),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

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
                                enabled = generatedCaption.isNotBlank(),
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
                            text = generatedCaption.ifBlank {
                                "Your AI caption will appear here after you analyze your actual ride footage."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (generatedCaption.isBlank()) Color(0xFF94A3B8) else Color(0xFFE2E8F0)
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
                                    if (!proBillingState.isPro) {
                                        viewModel.navigateTo(Screen.PRO_PAYWALL)
                                    } else if (!isGenerating) {
                                        isGenerating = true
                                        aiError = null
                                        coroutineScope.launch {
                                            try {
                                                val selected = highlightClips.filter { it.isSelected }
                                                if (selectedVideoUri != null && analyzedVideoUri != selectedVideoUri) {
                                                    aiError = "Run AI Find Highlights for the selected video before generating its caption."
                                                } else if (selected.isEmpty()) {
                                                    aiError = "Choose a video and run AI Find Highlights before generating a story."
                                                } else {
                                                    val clipContext = selected.map {
                                                        RideStoryAiService.ClipContext(
                                                            timeRange = it.timestamp,
                                                            title = it.title,
                                                            tag = it.tag,
                                                            speedKmh = it.speedKmh.takeIf { speed -> speed > 0 },
                                                            altitudeM = it.altitudeM.takeIf { altitude -> altitude > 0 }
                                                        )
                                                    }
                                                    generatedCaption = if (selectedVideoUri != null) {
                                                        RideStoryAiService.analyzeVideoAndGenerateCaption(
                                                            context = context,
                                                            videoUri = selectedVideoUri!!,
                                                            region = selectedRegion,
                                                            targetLength = targetLength,
                                                            narrationLanguage = narrationLang,
                                                            clips = clipContext
                                                        )
                                                    } else {
                                                        RideStoryAiService.generateCaption(
                                                            region = selectedRegion,
                                                            targetLength = targetLength,
                                                            narrationLanguage = narrationLang,
                                                            clips = clipContext
                                                        )
                                                    }
                                                }
                                            } catch (error: Exception) {
                                                aiError = when (error) {
                                                    is IllegalArgumentException -> error.message ?: "Check the selected video and highlights."
                                                    else -> "AI generation failed. Check Firebase Auth, App Check, deployed Cloud Functions and server secrets, then try again."
                                                }
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
                                enabled = generatedCaption.isNotBlank(),
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
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val source = selectedVideoUri
                                val selected = highlightClips.filter { it.isSelected }
                                if (source == null) {
                                    reelExportError = "Choose a ride video before exporting a reel."
                                } else if (analyzedVideoUri != source) {
                                    reelExportError = "Tap AI Find Highlights for this video first, then review the suggested timestamps."
                                } else if (selected.isEmpty()) {
                                    reelExportError = "Select at least one highlight before exporting."
                                } else if (!isExportingReel) {
                                    isExportingReel = true
                                    reelExportError = null
                                    coroutineScope.launch {
                                        try {
                                            val output = RideStoryVideoExporter.export(
                                                context = context,
                                                sourceUri = source,
                                                segments = selected.map {
                                                    RideStoryVideoExporter.Segment(
                                                        timeRange = it.timestamp,
                                                        title = it.title
                                                    )
                                                },
                                                targetLength = targetLength
                                            )
                                            pendingVideoFile = output
                                            reelFilePicker.launch("adventurer-ride-story.mp4")
                                        } catch (error: Exception) {
                                            reelExportError = error.message ?: "Video export failed. Try a different source video."
                                        } finally {
                                            isExportingReel = false
                                        }
                                    }
                                }
                            },
                            enabled = !isExportingReel,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HighAltitudeCyan)
                        ) {
                            if (isExportingReel) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rendering highlight reel…", color = Color.Black)
                            } else {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Render & Export MP4 Reel", color = Color.Black)
                            }
                        }
                        if (reelExportError != null) {
                            Text(
                                text = reelExportError!!,
                                color = Color(0xFFFCA5A5),
                                style = MaterialTheme.typography.bodySmall
                            )
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
                        enabled = highlightClips.any { it.isSelected },
                        onClick = {
                            val selectedClips = highlightClips.filter { it.isSelected }
                            if (selectedClips.isEmpty()) {
                                Toast.makeText(context, "Select at least one highlight to export.", Toast.LENGTH_SHORT).show()
                            } else {
                                pendingEdlJson = JSONObject()
                                    .put("format", "adventurer-edl-v1")
                                    .put("region", selectedRegion)
                                    .put("targetLength", targetLength)
                                    .put("narrationLanguage", narrationLang)
                                    .put("sourceVideoSelected", selectedVideoUri != null)
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
                                edlFilePicker.launch("adventurer-ride-story.edl.json")
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

            if (highlightClips.isEmpty()) {
                item {
                    Text(
                        "No highlights yet. Choose a real ride video and tap AI Find Highlights to build an editable timeline from that footage.",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            }

            if (highlightClips.isEmpty()) {
                item {
                    Text(
                        "No highlights yet. Choose real ride footage and tap AI Find Highlights to build an editable timeline from that video.",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
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
                                text = if (clip.speedKmh > 0 && clip.altitudeM > 0) "⏱️ ${clip.timestamp} • Speed: ${clip.speedKmh} km/h • Alt: ${clip.altitudeM}m" else "⏱️ ${clip.timestamp} • AI-suggested moment • telemetry unavailable",
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
