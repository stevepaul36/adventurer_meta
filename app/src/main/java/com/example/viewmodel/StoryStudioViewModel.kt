package com.example.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.story.AnalysisResult
import com.example.data.story.MetaAiClient
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

enum class StudioBusy { ANALYZING, AI_LABELING, EXPORTING, WRITING }

data class StudioState(
    val sourceUri: Uri? = null,
    val sourceName: String = "",
    val info: VideoAnalyzer.VideoInfo? = null,
    val title: String = "My ride, my story",
    val location: String = "Western Ghats, India",
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
    val aiConfigured: Boolean = false,
    val aiModel: String = "",
) {
    val selected: List<StudioMoment> get() = moments.filter { it.selected }.sortedBy { it.startSec }
    val reelSec: Double get() = selected.sumOf { it.durationSec }
}

class StoryStudioViewModel(app: Application) : AndroidViewModel(app) {

    private val analyzer = VideoAnalyzer(app)
    private val exporter = ReelExporter(app)
    private val ai = MetaAiClient()

    private val _state = MutableStateFlow(StudioState(aiConfigured = ai.isConfigured, aiModel = ai.visionModel))
    val state: StateFlow<StudioState> = _state.asStateFlow()

    private var job: Job? = null

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

    fun newCaptureUri(): Uri {
        val dir = File(getApplication<Application>().cacheDir, "ridestory/captures").apply { mkdirs() }
        dir.listFiles()?.filter { it.length() == 0L }?.forEach { it.delete() }
        val file = File(dir, "ride-${System.currentTimeMillis()}.mp4")
        return FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", file)
    }

    fun setTitle(v: String) = _state.update { it.copy(title = v.take(80)) }
    fun setLocation(v: String) = _state.update { it.copy(location = v.take(80)) }
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

    fun analyze() {
        val s = _state.value
        val uri = s.sourceUri ?: return
        launchTask(StudioBusy.ANALYZING, "Reading footage") {
            val result = analyzer.analyze(uri, s.targetSec.toDouble()) { p, stage -> progress(p, stage) }
            _state.update { it.copy(analysis = result, moments = result.moments, reel = null, story = null) }
        }
    }

    fun enhanceWithAi() {
        val s = _state.value
        if (!ai.isConfigured || s.moments.isEmpty()) return
        launchTask(StudioBusy.AI_LABELING, "Asking ${ai.visionModel} to review ${minOf(12, s.moments.size)} keyframes") {
            progress(0.3f, "Asking ${ai.visionModel} to review keyframes")
            val labels = ai.labelMoments(s.moments, s.location).associateBy { it.id }
            val merged = s.moments.map { m ->
                val l = labels[m.id] ?: return@map m
                m.copy(
                    label = l.label.ifBlank { m.label },
                    description = l.description.ifBlank { m.description },
                    tags = (l.tags + m.tags.filter { it == "bookmarked" }).distinct(),
                    score = 0.55 * m.score + 0.45 * (l.score / 10.0),
                    aiLabeled = true,
                    privacyFaces = l.faces,
                    privacyPlates = l.plates,
                )
            }
            _state.update { it.copy(moments = MomentPicker.reselect(merged, it.targetSec.toDouble()), reel = null) }
        }
    }

    fun exportReel() {
        val s = _state.value
        val uri = s.sourceUri ?: return
        val clips = s.selected
        if (clips.isEmpty()) return
        val labels = s.story?.chapterTitles?.takeIf { it.size == clips.size } ?: clips.map { it.label }
        launchTask(StudioBusy.EXPORTING, "Rendering ${clips.size} clips") {
            val file = exporter.export(uri, clips, labels, s.title, s.location, s.aspect) { p ->
                progress(p, "Rendering ${s.aspect.label} · ${(p * 100).toInt()}%")
            }
            _state.update { it.copy(reel = file) }
        }
    }

    fun writeStory() {
        val s = _state.value
        val clips = s.selected
        if (clips.isEmpty()) return
        val sourceSec = s.analysis?.durationSec ?: 0.0
        launchTask(StudioBusy.WRITING, "Writing your story") {
            val story = if (ai.isConfigured) {
                try {
                    ai.writeStory(s.title, s.location, s.language.displayName, clips, sourceSec)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _state.update { it.copy(error = "AI story failed, used the offline template: ${e.message}") }
                    StoryTemplate.build(s.title, s.location, clips, sourceSec)
                }
            } else {
                StoryTemplate.build(s.title, s.location, clips, sourceSec)
            }
            _state.update { it.copy(story = story) }
        }
    }

    fun cancel() {
        job?.cancel()
        _state.update { it.copy(busy = null) }
    }

    fun reelShareUri(): Uri? = _state.value.reel?.let {
        FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", it)
    }

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
