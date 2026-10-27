package com.example.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.RideStoryAiService
import com.example.data.model.ActivityType
import com.example.data.story.AdventureStoryTemplates
import com.example.data.story.AnalysisResult
import com.example.data.story.MomentPicker
import com.example.data.story.ReelExporter
import com.example.data.story.StoryCopy
import com.example.data.story.StoryTemplate
import com.example.data.story.StudioMoment
import com.example.data.story.VideoAnalyzer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import com.google.firebase.FirebaseApp

enum class StudioBusy { ANALYZING, AI_LABELING, EXPORTING, WRITING }

data class StudioState(
    val sourceUri: Uri? = null,
    val sourceName: String = "",
    val info: VideoAnalyzer.VideoInfo? = null,
    val title: String = "My ride, my story",
    val location: String = "",
    val activityType: ActivityType = ActivityType.Motorcycling,
    val templateId: String = "epic_journey",
    val language: SupportedLang = SupportedLang.ENGLISH,
    val targetSec: Int = 60,
    val aspect: ReelExporter.Aspect = ReelExporter.Aspect.VERTICAL,
    val analysis: AnalysisResult? = null,
    val moments: List<StudioMoment> = emptyList(),
    val busy: StudioBusy? = null,
    val progress: Float = 0f,
    val stage: String = "",
    val error: String? = null,
    val reel: File? = null,
    val story: StoryCopy? = null,
    val firebaseConfigured: Boolean = false,
) {
    val selected: List<StudioMoment> get() = moments.filter { it.selected }.sortedBy { it.startSec }
    val reelSec: Double get() = selected.sumOf { it.durationSec }
}

/**
 * Adventure Story Studio: on-device video scoring/export → optional secure cloud highlight suggestions →
 * server-generated editable copy (with a local template fallback).
 */
class StoryStudioViewModel(app: Application) : AndroidViewModel(app) {

    private val analyzer = VideoAnalyzer(app)
    private val exporter = ReelExporter(app)
    private val _state = MutableStateFlow(
        StudioState(firebaseConfigured = runCatching { FirebaseApp.getApps(app).isNotEmpty() }.getOrDefault(false))
    )
    val state: StateFlow<StudioState> = _state.asStateFlow()

    private var job: Job? = null

    // ---------------------------------------------------------------- inputs

    fun setSource(uri: Uri, name: String) {
        job?.cancel()
        _state.update {
            it.copy(sourceUri = uri, sourceName = name, info = null, analysis = null, moments = emptyList(), reel = null, story = null, busy = null, error = null)
        }
        viewModelScope.launch {
            val info = withContext(Dispatchers.IO) { runCatching { analyzer.probe(uri) }.getOrNull() }
            if (_state.value.sourceUri != uri) return@launch
            _state.update {
                it.copy(info = info, error = if (info == null || info.durationSec <= 0) "This video can't be read. Try an MP4 recorded on the phone." else null)
            }
        }
    }

    /** A FileProvider Uri the system camera can record into. */
    fun newCaptureUri(): Uri {
        val dir = File(getApplication<Application>().cacheDir, "ridestory/captures").apply { mkdirs() }
        dir.listFiles()?.filter { it.length() == 0L }?.forEach { it.delete() }
        val file = File(dir, "ride-${System.currentTimeMillis()}.mp4")
        return FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", file)
    }

    fun setTitle(v: String) = _state.update { it.copy(title = v.take(80), story = null) }
    fun setLocation(v: String) = _state.update { it.copy(location = v.take(80), story = null) }
    fun setActivityType(v: ActivityType) = _state.update {
        if (it.activityType == v) it else {
            val supported = AdventureStoryTemplates.forActivity(v)
            val nextTemplate = supported.firstOrNull { template -> template.id == it.templateId }?.id
                ?: supported.firstOrNull()?.id
                ?: "epic_journey"
            it.copy(activityType = v, templateId = nextTemplate, story = null)
        }
    }
    fun setTemplate(id: String) = _state.update {
        if (it.templateId == id) it else it.copy(templateId = id, story = null)
    }
    fun setLanguage(v: SupportedLang) = _state.update { it.copy(language = v) }
    fun setAspect(v: ReelExporter.Aspect) = _state.update { it.copy(aspect = v, reel = null) }
    fun setTarget(sec: Int) = _state.update { s ->
        s.copy(targetSec = sec, moments = if (s.moments.isEmpty()) s.moments else MomentPicker.reselect(s.moments, sec.toDouble()), reel = null)
    }

    fun toggleMoment(id: String) = _state.update { s ->
        s.copy(moments = s.moments.map { if (it.id == id) it.copy(selected = !it.selected) else it }, reel = null)
    }

    fun updateCaption(v: String) = _state.update { s -> s.copy(story = s.story?.copy(caption = v)) }
    fun updateDiary(v: String) = _state.update { s -> s.copy(story = s.story?.copy(diary = v)) }
    fun clearError() = _state.update { it.copy(error = null) }

    // ---------------------------------------------------------------- actions

    fun analyze() {
        val s = _state.value
        val uri = s.sourceUri
        if (uri == null) {
            _state.update { it.copy(error = "Choose or record a video first. Nothing has been changed.") }
            return
        }
        launchTask(StudioBusy.ANALYZING, "Reading footage on this phone") {
            val result = analyzer.analyze(uri, s.targetSec.toDouble()) { p, stage -> progress(p, stage) }
            _state.update { it.copy(analysis = result, moments = result.moments, reel = null, story = null) }
        }
    }

    /** Explicitly uploads the chosen video for server-side highlight suggestions. Local moments stay if cloud AI fails. */
    fun enhanceWithAi(activityType: ActivityType? = null) {
        val current = _state.value
        val s = if (activityType != null) current.copy(activityType = activityType) else current
        if (s.activityType != current.activityType) _state.value = s
        val uri = s.sourceUri
        if (uri == null || s.moments.isEmpty()) {
            _state.update { it.copy(error = "Choose a video and run local analysis before requesting cloud highlights.") }
            return
        }
        val sizeBytes = s.info?.sizeBytes
        if (sizeBytes != null && sizeBytes > 14L * 1024 * 1024) {
            _state.update { it.copy(error = "Cloud highlight review supports videos up to 14 MB. Your on-device highlights are unchanged.") }
            return
        }
        launchTask(StudioBusy.AI_LABELING, "Securely requesting cloud highlight suggestions") {
            progress(0.12f, "Uploading the selected video to the secure backend")
            val targetLength = when {
                s.targetSec <= 15 -> "15s Reel"
                s.targetSec <= 30 -> "30s Reel"
                else -> "60s Reel"
            }
            val suggestions = RideStoryAiService.analyzeVideoForHighlights(
                context = getApplication(),
                videoUri = uri,
                targetLength = targetLength,
                narrationLanguage = s.language.displayName,
                activityType = s.activityType.displayName,
            )
            if (suggestions.isEmpty()) {
                _state.update { it.copy(error = "Cloud AI did not return reliable highlights. Your on-device moments are unchanged.") }
                return@launchTask
            }
            val usedIds = mutableSetOf<String>()
            val replacements = linkedMapOf<String, StudioMoment>()
            suggestions.forEach { suggestion ->
                val midpoint = (suggestion.startSeconds + suggestion.endSeconds) / 2.0
                val nearest = s.moments
                    .filterNot { it.id in usedIds }
                    .minByOrNull { abs(it.peakSec - midpoint) }
                    ?: return@forEach
                usedIds += nearest.id
                replacements[nearest.id] = nearest.copy(
                    label = suggestion.title.ifBlank { nearest.label },
                    description = "Cloud AI suggested a highlight around ${suggestion.timeRange}. Review the clip before exporting.",
                    tags = (nearest.tags + suggestion.tag.lowercase()).distinct().take(6),
                    score = maxOf(nearest.score, 0.85),
                    aiLabeled = true,
                )
            }
            if (replacements.isEmpty()) {
                _state.update { it.copy(error = "Cloud AI returned highlights, but they could not be matched to local moments. Your local analysis is unchanged.") }
                return@launchTask
            }
            _state.update { latest ->
                latest.copy(
                    moments = latest.moments.map { replacements[it.id] ?: it },
                    reel = null,
                    story = null,
                    error = "Cloud suggestions added to ${replacements.size} moments. Review your selections before exporting.",
                )
            }
        }
    }

    fun exportReel() {
        val s = _state.value
        val uri = s.sourceUri
        if (uri == null) {
            _state.update { it.copy(error = "Choose or record a video before exporting.") }
            return
        }
        val clips = s.selected
        if (clips.isEmpty()) {
            _state.update { it.copy(error = "Select at least one highlight before exporting.") }
            return
        }
        val labels = s.story?.chapterTitles?.takeIf { it.size == clips.size } ?: clips.map { it.label }
        launchTask(StudioBusy.EXPORTING, "Rendering ${clips.size} clips") {
            val file = exporter.export(uri, clips, labels, s.title, s.location, s.aspect) { p ->
                progress(p, "Rendering ${s.aspect.label} · ${(p * 100).toInt()}%")
            }
            _state.update { it.copy(reel = file) }
        }
    }

    fun writeStory(activityType: ActivityType? = null) {
        val current = _state.value
        val s = if (activityType != null) current.copy(activityType = activityType) else current
        if (s.activityType != current.activityType) _state.value = s
        val clips = s.selected
        if (clips.isEmpty()) {
            _state.update { it.copy(error = "Select at least one moment in Highlights before writing your story.") }
            return
        }
        val sourceSec = s.analysis?.durationSec ?: s.info?.durationSec ?: 0.0
        val template = AdventureStoryTemplates.byId(s.templateId)
        launchTask(StudioBusy.WRITING, "Writing your ${s.activityType.displayName.lowercase()} story") {
            val story = try {
                RideStoryAiService.generateAdventureStory(
                    title = s.title,
                    location = s.location,
                    language = s.language.displayName,
                    activityName = s.activityType.displayName,
                    template = template,
                    moments = clips,
                    sourceDurationSec = sourceSec,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val reason = when {
                    e.message.orEmpty().contains("permission-denied", ignoreCase = true) ||
                        e.message.orEmpty().contains("Pro subscription", ignoreCase = true) ->
                        "Cloud AI needs an eligible Adventurer Pro account."
                    e.message.orEmpty().contains("failed-precondition", ignoreCase = true) ||
                        e.message.orEmpty().contains("Firebase", ignoreCase = true) ||
                        e.message.orEmpty().contains("unauthenticated", ignoreCase = true) ->
                        "Firebase sign-in or server AI is not configured yet."
                    else -> "The cloud story request failed."
                }
                _state.update { it.copy(error = "$reason Your editable offline story template is ready instead.") }
                StoryTemplate.build(s.title, s.location, clips, sourceSec, s.activityType, template)
            }
            _state.update { it.copy(story = story) }
        }
    }

    fun cancel() {
        job?.cancel()
        _state.update { it.copy(busy = null) }
    }

    /** Uri other apps can read (for the share sheet). */
    fun reelShareUri(): Uri? = _state.value.reel?.let {
        FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", it)
    }

    /** Copies the reel into Movies/Adventurer. Returns false on Android 9 and below (use Share instead). */
    suspend fun saveReelToGallery(): Boolean = withContext(Dispatchers.IO) {
        val file = _state.value.reel ?: return@withContext false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext false
        val resolver = getApplication<Application>().contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Adventurer")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext false
        try {
            resolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } } ?: error("no stream")
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    // ---------------------------------------------------------------- helpers

    private fun progress(p: Float, stage: String) = _state.update { it.copy(progress = p.coerceIn(0f, 1f), stage = stage) }

    private fun launchTask(kind: StudioBusy, stage: String, block: suspend () -> Unit) {
        if (_state.value.busy != null) return
        job = viewModelScope.launch {
            _state.update { it.copy(busy = kind, progress = 0f, stage = stage, error = null) }
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: e.javaClass.simpleName) }
            } finally {
                _state.update { it.copy(busy = null) }
            }
        }
    }
}
