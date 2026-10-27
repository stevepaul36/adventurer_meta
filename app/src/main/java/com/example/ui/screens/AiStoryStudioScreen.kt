@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.ActivityType
import com.example.data.billing.AdventurerProBilling
import com.example.analytics.AdventureFunnelAnalytics
import com.example.data.story.AdventureStoryTemplates
import com.example.data.story.AnalysisResult
import com.example.data.story.ReelExporter
import com.example.data.story.StoryTemplate
import com.example.data.story.StudioMoment
import com.example.data.story.formatTime
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen
import com.example.viewmodel.StoryStudioViewModel
import com.example.viewmodel.StudioBusy
import com.example.viewmodel.StudioState
import com.example.viewmodel.SupportedLang
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

private val Panel = Color(0xFF131B2A)
private val PanelBorder = Color(0xFF334155)
private val Muted = Color(0xFF94A3B8)
private val Violet = Color(0xFFC084FC)

@Composable
fun AiStoryStudioScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier,
    studio: StoryStudioViewModel = viewModel(),
) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by studio.state.collectAsState()
    val activityMode by viewModel.activityMode.collectAsState()
    val proState by AdventurerProBilling.state.collectAsState()
    var tab by rememberSaveable { mutableStateOf(0) }
    var lastLoggedReelPath by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(activityMode) { studio.setActivityType(activityMode) }
    LaunchedEffect(state.reel?.absolutePath) {
        val path = state.reel?.absolutePath.orEmpty()
        if (path.isNotBlank() && path != lastLoggedReelPath) {
            AdventureFunnelAnalytics.log(context, "story_exported", mapOf("activity_type" to activityMode.name, "target_seconds" to state.targetSec))
            lastLoggedReelPath = path
        }
    }

    // --- Pick / record source video
    var pendingCapture by rememberSaveable { mutableStateOf<Uri?>(null) }
    val captureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { ok ->
        val uri = pendingCapture
        if (ok && uri != null) studio.setSource(uri, "Camera recording")
        pendingCapture = null
    }
    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) studio.setSource(uri, displayName(context, uri))
    }

    // --- Text-to-speech for the narration
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var speaking by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { speaking = false }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { speaking = false }
                })
                tts = engine
            }
        }
        onDispose { engine?.stop(); engine?.shutdown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (activityMode == ActivityType.Motorcycling) "RideStory AI Studio"
                            else "${activityMode.displayName} Story Studio",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            if (state.aiConfigured) "On-device scoring · Meta AI · ${state.aiModel}" else "On-device scoring · AI off (add MODEL_API_KEY)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.aiConfigured) Violet else AmberGold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (proState.isPro) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberGold.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                "PRO",
                                color = AmberGold,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.PRO_PAYWALL) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text("PRO", color = AmberGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F17)),
            )
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier,
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = tab, containerColor = Panel, contentColor = AdventureOrange, divider = { HorizontalDivider(color = PanelBorder) }) {
                listOf("1 · Footage", "2 · Highlights", "3 · Story").forEachIndexed { i, title ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = {
                        Text(title, fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal, color = if (tab == i) AdventureOrange else Muted)
                    })
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                state.busy?.let { busy ->
                    item { BusyCard(state, onCancel = if (busy == StudioBusy.ANALYZING || busy == StudioBusy.EXPORTING) studio::cancel else null) }
                }
                state.error?.let { err ->
                    item { ErrorCard(err, studio::clearError) }
                }

                when (tab) {
                    0 -> footageTab(
                        state = state,
                        activityMode = activityMode,
                        onRecord = { studio.newCaptureUri().also { pendingCapture = it; captureLauncher.launch(it) } },
                        onPick = { pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                        studio = studio,
                        onAnalyze = { studio.analyze(); tab = 1 },
                    )
                    1 -> highlightsTab(
                        state = state,
                        studio = studio,
                        activityMode = activityMode,
                        onGoToFootage = { tab = 0 },
                        onShare = { shareReel(context, studio.reelShareUri(), state) },
                        onSave = {
                            scope.launch {
                                val ok = studio.saveReelToGallery()
                                Toast.makeText(context, if (ok) "Saved to Movies/Adventurer" else "Couldn't save — use Share instead", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onWriteStory = { tab = 2; if (state.story == null) studio.writeStory(activityMode) },
                    )
                    else -> storyTab(
                        state = state,
                        studio = studio,
                        activityMode = activityMode,
                        speaking = speaking,
                        onSpeak = {
                            val story = state.story ?: return@storyTab
                            val engine = tts
                            if (engine == null) {
                                Toast.makeText(context, "Text-to-speech is not ready yet", Toast.LENGTH_SHORT).show()
                            } else if (speaking) {
                                engine.stop(); speaking = false
                            } else {
                                val result = engine.setLanguage(Locale.forLanguageTag("${state.language.code}-IN"))
                                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                    engine.setLanguage(Locale.forLanguageTag("en-IN"))
                                    Toast.makeText(context, "${state.language.displayName} voice not installed — install it in Settings › Text-to-speech", Toast.LENGTH_LONG).show()
                                }
                                engine.speak(story.narration.ifBlank { story.caption }, TextToSpeech.QUEUE_FLUSH, null, "ridestory")
                                speaking = true
                            }
                        },
                        onCopy = { text ->
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("RideStory caption", text))
                            Toast.makeText(context, "Caption copied", Toast.LENGTH_SHORT).show()
                        },
                        onPost = { caption ->
                            val cover = state.selected.firstOrNull()?.thumbnailPath?.let { Uri.fromFile(File(it)).toString() }
                            viewModel.createPost(caption = caption, imageUrl = cover)
                            AdventureFunnelAnalytics.log(context, "story_shared", mapOf("share_target" to "in_app_feed", "activity_type" to activityMode.name))
                            Toast.makeText(context, "Story posted to your feed", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(Screen.FEED)
                        },
                        onGoToHighlights = { tab = 1 },
                    )
                }
            }
        }
    }
}

// ============================================================================ Tab 1 — footage

private fun androidx.compose.foundation.lazy.LazyListScope.footageTab(
    state: StudioState,
    activityMode: ActivityType,
    onRecord: () -> Unit,
    onPick: () -> Unit,
    studio: StoryStudioViewModel,
    onAnalyze: () -> Unit,
) {
    item {
        PanelCard {
            Text(
                if (activityMode == ActivityType.Motorcycling) "Ride footage" else "${activityMode.displayName} footage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Analysis runs on this phone — the video is never uploaded. Only small keyframes go to Meta AI, and only when you tap Enhance.",
                style = MaterialTheme.typography.bodySmall, color = Muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onRecord, colors = ButtonDefaults.buttonColors(containerColor = DangerRed), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).testTag("record_ride_button")) {
                    Icon(Icons.Default.Videocam, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (activityMode == ActivityType.Motorcycling) "Record ride" else "Record footage", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onPick, colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).testTag("pick_video_button")) {
                    Icon(Icons.Default.VideoLibrary, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text(if (state.sourceUri == null) "Pick video" else "Replace", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            state.sourceUri?.let { uri ->
                VideoPlayer(uri = uri, modifier = Modifier.fillMaxWidth().height(210.dp))
                val info = state.info
                Text(
                    buildString {
                        append(state.sourceName)
                        if (info != null) {
                            append(" · ${formatTime(info.durationSec)} · ${info.width}×${info.height}")
                            info.sizeBytes?.let { append(" · %.1f MB".format(it / 1_048_576.0)) }
                        }
                    },
                    style = MaterialTheme.typography.labelSmall, color = Muted,
                )
            }
        }
    }

    item {
        PanelCard {
            Text("Reel settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            StudioTextField("Story title", state.title, studio::setTitle)
            StudioTextField("Ride location", state.location, studio::setLocation)
            FieldLabel("Reel length")
            ChipRow(listOf(30, 60, 90, 120, 180), state.targetSec, { if (it < 60) "${it}s" else if (it % 60 == 0) "${it / 60} min" else "${it}s" }, studio::setTarget)
            FieldLabel("Format")
            ChipRow(ReelExporter.Aspect.entries, state.aspect, { it.label }, studio::setAspect)
            FieldLabel("Story language")
            ChipRow(SupportedLang.entries, state.language, { it.localName }, studio::setLanguage)
            Button(
                onClick = onAnalyze,
                enabled = state.sourceUri != null && state.info != null && state.busy == null,
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("analyze_button"),
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(if (state.analysis == null) "Find the best moments" else "Re-analyse footage", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============================================================================ Tab 2 — highlights

private fun androidx.compose.foundation.lazy.LazyListScope.highlightsTab(
    state: StudioState,
    studio: StoryStudioViewModel,
    activityMode: ActivityType,
    onGoToFootage: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onWriteStory: () -> Unit,
) {
    val analysis = state.analysis
    if (analysis == null) {
        item {
            EmptyCard(
                if (state.busy == StudioBusy.ANALYZING) "Scanning your footage…" else "Pick or record adventure footage, then tap “Find the best moments”.",
                if (state.busy == null) "Go to footage" else null, onGoToFootage,
            )
        }
        return
    }

    item {
        PanelCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Moment timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "${analysis.samples.size} frames scored · ${analysis.sceneCutsSec.size} scene changes · reel ${formatTime(state.reelSec)} / ${formatTime(state.targetSec.toDouble())}",
                        style = MaterialTheme.typography.labelSmall, color = Muted,
                    )
                }
            }
            Timeline(analysis, state.moments, Modifier.fillMaxWidth().height(72.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { studio.enhanceWithAi(activityMode) },
                    enabled = state.aiConfigured && state.busy == null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("enhance_ai_button"),
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text("Enhance with Meta AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(
                    onClick = studio::exportReel,
                    enabled = state.selected.isNotEmpty() && state.busy == null,
                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("render_reel_button"),
                ) {
                    Icon(Icons.Default.Movie, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text("Render reel", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            if (!state.aiConfigured) {
                Text("Add MODEL_API_KEY to .env and rebuild to let Meta AI name, re-rank and privacy-check your moments.", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        }
    }

    state.reel?.let { reel ->
        item {
            PanelCard(border = Color(0xFF10B981)) {
                Text("Reel ready · ${state.aspect.label} · %.1f MB".format(reel.length() / 1_048_576.0), fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                VideoPlayer(
                    uri = Uri.fromFile(reel),
                    modifier = Modifier.fillMaxWidth().height(if (state.aspect == ReelExporter.Aspect.VERTICAL) 360.dp else 210.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onSave, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Download, null, tint = HighAltitudeCyan, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Save", color = HighAltitudeCyan)
                    }
                    OutlinedButton(onClick = onShare, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Share", color = Color.White)
                    }
                    Button(onClick = onWriteStory, colors = ButtonDefaults.buttonColors(containerColor = AmberGold), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                        Text("Story →", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    item {
        Text("Moments (${state.selected.size} of ${state.moments.size} in reel)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
    items(state.moments, key = { it.id }) { m -> MomentCard(m) { studio.toggleMoment(m.id) } }
}

// ============================================================================ Tab 3 — story

private fun androidx.compose.foundation.lazy.LazyListScope.storyTab(
    state: StudioState,
    studio: StoryStudioViewModel,
    activityMode: ActivityType,
    speaking: Boolean,
    onSpeak: () -> Unit,
    onCopy: (String) -> Unit,
    onPost: (String) -> Unit,
    onGoToHighlights: () -> Unit,
) {
    item {
        PanelCard {
            Text("Story template · " + activityMode.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Choose a style before writing. The template changes the narration structure; only selected footage and supplied facts are used.", color = Muted, style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdventureStoryTemplates.forActivity(activityMode).forEach { template ->
                    FilterChip(
                        selected = state.templateId == template.id,
                        onClick = { studio.setTemplate(template.id) },
                        label = { Text(template.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AdventureOrange, selectedLabelColor = Color.Black,
                            containerColor = Panel, labelColor = Color.White,
                        ),
                    )
                }
            }
            Text(AdventureStoryTemplates.byId(state.templateId).description, color = Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
    if (state.selected.isEmpty()) {
        item { EmptyCard("Your story is written from the moments you keep. Analyse your footage first.", "Go to highlights", onGoToHighlights) }
        return
    }
    item {
        PanelCard {
            Text("Travel story · ${state.language.displayName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                if (state.aiConfigured) "Written by Meta AI from your ${state.selected.size} moments." else "Offline template from your moments — add a model key for AI writing in Indian languages.",
                style = MaterialTheme.typography.labelSmall, color = Muted,
            )
            Button(
                onClick = { studio.writeStory(activityMode) },
                enabled = state.busy == null,
                colors = ButtonDefaults.buttonColors(containerColor = HighAltitudeCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("write_story_button"),
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (state.story == null) "Write my story" else "Rewrite", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
    val story = state.story ?: return
    val shareText = story.caption + "\n\n" + story.hashtags.joinToString(" ")

    item {
        PanelCard(border = Color(0xFF8B5CF6)) {
            FieldLabel(if (story.fromAi) "Caption" else "Caption (template)")
            OutlinedTextField(value = story.caption, onValueChange = studio::updateCaption, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White), colors = fieldColors())
            Text(story.hashtags.joinToString(" "), style = MaterialTheme.typography.bodySmall, color = HighAltitudeCyan)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onCopy(shareText) }, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ContentCopy, null, tint = Violet, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Copy", color = Violet)
                }
                Button(onClick = { onPost(shareText) }, colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, null, tint = Color.Black, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Post to feed", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    item {
        PanelCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Diary & narration", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Button(onClick = onSpeak, colors = ButtonDefaults.buttonColors(containerColor = if (speaking) DangerRed else AdventureOrange), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Icon(if (speaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp)); Text(if (speaking) "Stop" else "Play narration", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
            OutlinedTextField(value = story.diary, onValueChange = studio::updateDiary, modifier = Modifier.fillMaxWidth(), minLines = 5, textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1), lineHeight = 21.sp), colors = fieldColors())
            if (story.narration.isNotBlank()) {
                FieldLabel("Voice-over script")
                Text(story.narration, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
            }
        }
    }
    item { Text("Chapters (reel time)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White) }
    val times = StoryTemplate.reelChapterTimes(state.selected)
    items(state.selected.indices.toList()) { i ->
        Surface(shape = RoundedCornerShape(10.dp), color = Panel, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = AmberGold.copy(alpha = 0.2f)) {
                    Text(formatTime(times[i]), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AmberGold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(story.chapterTitles.getOrElse(i) { state.selected[i].label }, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ============================================================================ pieces

@Composable
private fun MomentCard(m: StudioMoment, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (m.selected) Panel else Panel.copy(alpha = 0.55f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (m.selected) AdventureOrange else PanelBorder)),
    ) {
        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.width(120.dp).height(68.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black)) {
                m.thumbnailPath?.let { AsyncImage(model = File(it), contentDescription = m.label, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                Text(
                    "${formatTime(m.startSec)}–${formatTime(m.endSec)}",
                    color = Color.White, fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.BottomStart).padding(3.dp).background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp),
                )
                if (m.aiLabeled) {
                    Text("AI", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopStart).padding(3.dp).background(Color(0xFF9333EA), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(m.label, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Checkbox(checked = m.selected, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = AdventureOrange), modifier = Modifier.size(28.dp))
                }
                LinearProgressIndicator(progress = { m.score.toFloat().coerceIn(0f, 1f) }, color = AmberGold, trackColor = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape))
                Text(m.description, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 2)
                if (m.tags.isNotEmpty()) Text(m.tags.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = Color(0xFFCBD5E1))
                if (m.privacyFaces || m.privacyPlates) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = DangerRed, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            listOfNotNull("faces".takeIf { m.privacyFaces }, "licence plates".takeIf { m.privacyPlates }).joinToString(" & ") + " visible — check consent",
                            style = MaterialTheme.typography.labelSmall, color = DangerRed,
                        )
                    }
                }
            }
        }
    }
}

/** Score curve across the ride; amber = in reel, grey = alternates, lines = scene cuts. */
@Composable
private fun Timeline(analysis: AnalysisResult, moments: List<StudioMoment>, modifier: Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF0F172A))) {
        val d = analysis.durationSec.coerceAtLeast(0.1)
        fun x(t: Double) = (t / d * size.width).toFloat()
        analysis.sceneCutsSec.forEach { c -> drawLine(PanelBorder, Offset(x(c), 0f), Offset(x(c), size.height), strokeWidth = 1.5f) }
        moments.forEach { m ->
            drawRect(
                color = if (m.selected) AdventureOrange.copy(alpha = 0.35f) else Muted.copy(alpha = 0.15f),
                topLeft = Offset(x(m.startSec), 0f),
                size = Size((x(m.endSec) - x(m.startSec)).coerceAtLeast(2f), size.height),
            )
        }
        val path = Path()
        analysis.samples.forEachIndexed { i, s ->
            val px = x(s.metrics.timeSec)
            val py = size.height - 6f - (s.score.toFloat() * (size.height - 12f))
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(path, color = Color(0xFFFB7185), style = Stroke(width = 3f))
    }
}

@Composable
private fun VideoPlayer(uri: Uri, modifier: Modifier) {
    var view by remember { mutableStateOf<VideoView?>(null) }
    var playing by remember(uri) { mutableStateOf(false) }
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(Color.Black).border(1.dp, PanelBorder, RoundedCornerShape(12.dp))) {
        AndroidView(
            factory = { ctx -> VideoView(ctx).also { v -> v.setOnCompletionListener { playing = false }; view = v } },
            update = { v ->
                if (v.tag != uri) {
                    v.tag = uri
                    v.setVideoURI(uri)
                    v.setOnPreparedListener { it.setVolume(1f, 1f); v.seekTo(1) }
                }
            },
            modifier = Modifier.align(Alignment.Center).fillMaxSize(),
        )
        FilledIconButton(
            onClick = {
                val v = view ?: return@FilledIconButton
                if (v.isPlaying) { v.pause(); playing = false } else { v.start(); playing = true }
            },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = 0.55f)),
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
        ) {
            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (playing) "Pause" else "Play", tint = Color.White)
        }
    }
    DisposableEffect(uri) { onDispose { view?.stopPlayback() } }
}

@Composable
private fun BusyCard(state: StudioState, onCancel: (() -> Unit)?) {
    PanelCard(border = AmberGold) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.stage.ifBlank { "Working…" }, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text("${(state.progress * 100).toInt()}%", color = AmberGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            if (onCancel != null) TextButton(onClick = onCancel) { Text("Cancel", color = Muted) }
        }
        if (state.busy == StudioBusy.AI_LABELING || state.busy == StudioBusy.WRITING) {
            LinearProgressIndicator(color = AmberGold, trackColor = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(progress = { state.progress }, color = AmberGold, trackColor = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ErrorCard(message: String, onDismiss: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = DangerRed.copy(alpha = 0.15f), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, color = Color(0xFFFECACA), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFFECACA)) }
        }
    }
}

@Composable
private fun EmptyCard(message: String, action: String?, onAction: () -> Unit) {
    PanelCard {
        Text(message, color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodyMedium)
        if (action != null) OutlinedButton(onClick = onAction, shape = RoundedCornerShape(10.dp)) { Text(action, color = Color.White) }
    }
}

@Composable
private fun PanelCard(border: Color = PanelBorder, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(border)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun FieldLabel(text: String) = Text(text, style = MaterialTheme.typography.labelSmall, color = Muted)

@Composable
private fun StudioTextField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        modifier = Modifier.fillMaxWidth(), colors = fieldColors(),
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = AdventureOrange, unfocusedBorderColor = PanelBorder,
    focusedLabelColor = AdventureOrange, unfocusedLabelColor = Muted, cursorColor = AdventureOrange,
)

@Composable
private fun <T> ChipRow(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { o ->
            FilterChip(
                selected = o == selected, onClick = { onSelect(o) }, label = { Text(label(o)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AdventureOrange, selectedLabelColor = Color.Black,
                    containerColor = Color(0xFF1E293B), labelColor = Color.White,
                ),
            )
        }
    }
}

private fun displayName(context: Context, uri: Uri): String =
    runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }.getOrNull() ?: "Ride video"

private fun shareReel(context: Context, uri: Uri?, state: StudioState) {
    if (uri == null) return
    val text = state.story?.let { it.caption + "\n\n" + it.hashtags.joinToString(" ") } ?: state.title
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    AdventureFunnelAnalytics.log(context, "story_share_sheet_opened", mapOf("share_target" to "android_share_sheet"))
    context.startActivity(Intent.createChooser(send, "Share your adventure reel"))
}
