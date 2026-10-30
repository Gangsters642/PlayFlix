package com.playflix.player.core.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.playflix.player.core.database.PlayFlixDao
import com.playflix.player.core.database.PlaybackHistoryEntity
import com.playflix.player.core.model.MediaItemModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class RepeatModeState {
    OFF, ALL, ONE
}

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class ActivePlaybackState(
    val currentMedia: MediaItemModel? = null,
    val queue: List<MediaItemModel> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isMiniPlayerVisible: Boolean = false,
    val isFullPlayerOpen: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatModeState = RepeatModeState.OFF,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerRemainingMs: Long = 0L,
    val formatBadge: String = "MP3  320kbps",
    val lyrics: List<LyricLine> = emptyList()
) {
    val nextUpMedia: MediaItemModel?
        get() = if (queue.size > 1) {
            val nextIdx = (currentIndex + 1) % queue.size
            queue.getOrNull(nextIdx)
        } else null
}

class PlayFlixPlayerController(
    private val context: Context,
    private val dao: PlayFlixDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val _playbackState = MutableStateFlow(ActivePlaybackState())
    val playbackState: StateFlow<ActivePlaybackState> = _playbackState.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = isPlaying,
                    durationMs = exoPlayer.duration.coerceAtLeast(_playbackState.value.currentMedia?.durationMs ?: 0L)
                )
                if (isPlaying) {
                    startProgressTicker()
                } else {
                    stopProgressTicker()
                    persistCurrentPosition()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                val queue = _playbackState.value.queue
                if (index in queue.indices) {
                    val nextTrack = queue[index]
                    _playbackState.value = _playbackState.value.copy(
                        currentMedia = nextTrack,
                        currentIndex = index,
                        currentPositionMs = 0L,
                        durationMs = nextTrack.durationMs,
                        isMiniPlayerVisible = true,
                        formatBadge = buildFormatBadge(nextTrack),
                        lyrics = loadLocalLyricsFor(nextTrack)
                    )
                    recordHistory(nextTrack, 0L)
                }
            }
        })
    }

    fun getPlayerInstance(): ExoPlayer = exoPlayer

    fun playMedia(
        item: MediaItemModel,
        queueList: List<MediaItemModel> = listOf(item),
        openFullPlayer: Boolean = false
    ) {
        startPlaybackServiceIfNeeded()

        val targetQueue = if (queueList.isEmpty()) listOf(item) else queueList
        val startIndex = targetQueue.indexOfFirst { it.id == item.id }.coerceAtLeast(0)

        val mediaItems = targetQueue.map { media ->
            MediaItem.Builder()
                .setMediaId(media.id.toString())
                .setUri(Uri.parse(media.contentUri))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(media.title)
                        .setArtist(media.artist)
                        .setAlbumTitle(media.album)
                        .setArtworkUri(media.artworkUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
        }

        exoPlayer.volume = 1.0f
        exoPlayer.setMediaItems(mediaItems, startIndex, item.lastPositionMs)
        exoPlayer.prepare()
        exoPlayer.play()

        _playbackState.value = _playbackState.value.copy(
            currentMedia = item,
            queue = targetQueue,
            currentIndex = startIndex,
            isPlaying = true,
            isMiniPlayerVisible = true,
            isFullPlayerOpen = openFullPlayer,
            currentPositionMs = item.lastPositionMs,
            durationMs = item.durationMs,
            formatBadge = buildFormatBadge(item),
            lyrics = loadLocalLyricsFor(item)
        )
        recordHistory(item, item.lastPositionMs)
    }

    fun openFullPlayer() {
        _playbackState.value = _playbackState.value.copy(isFullPlayerOpen = true)
    }

    fun closeFullPlayer() {
        _playbackState.value = _playbackState.value.copy(isFullPlayerOpen = false)
    }

    fun togglePlayPause() {
        exoPlayer.volume = 1.0f
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val safePos = positionMs.coerceIn(0L, _playbackState.value.durationMs.coerceAtLeast(1L))
        exoPlayer.seekTo(safePos)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = safePos)
    }

    fun skipNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_playbackState.value.queue.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
            exoPlayer.play()
        }
    }

    fun skipPrevious() {
        if (exoPlayer.currentPosition > 4000L) {
            exoPlayer.seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else {
            exoPlayer.seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val nextShuffle = !_playbackState.value.isShuffleEnabled
        exoPlayer.shuffleModeEnabled = nextShuffle
        _playbackState.value = _playbackState.value.copy(isShuffleEnabled = nextShuffle)
    }

    fun cycleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatModeState.OFF -> RepeatModeState.ALL
            RepeatModeState.ALL -> RepeatModeState.ONE
            RepeatModeState.ONE -> RepeatModeState.OFF
        }
        exoPlayer.repeatMode = when (nextMode) {
            RepeatModeState.OFF -> Player.REPEAT_MODE_OFF
            RepeatModeState.ALL -> Player.REPEAT_MODE_ALL
            RepeatModeState.ONE -> Player.REPEAT_MODE_ONE
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    fun setPlaybackSpeed(speed: Float) {
        val safeSpeed = speed.coerceIn(0.5f, 2.0f)
        exoPlayer.setPlaybackSpeed(safeSpeed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = safeSpeed)
    }

    fun playFromQueueIndex(index: Int) {
        val queue = _playbackState.value.queue
        if (index in queue.indices) {
            exoPlayer.seekTo(index, 0L)
            exoPlayer.play()
        }
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _playbackState.value.queue.toMutableList()
        if (index !in currentQueue.indices || currentQueue.size <= 1) return
        currentQueue.removeAt(index)
        exoPlayer.removeMediaItem(index)
        _playbackState.value = _playbackState.value.copy(
            queue = currentQueue,
            currentIndex = exoPlayer.currentMediaItemIndex.coerceAtLeast(0)
        )
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            exoPlayer.volume = 1.0f
            _playbackState.value = _playbackState.value.copy(sleepTimerRemainingMs = 0L)
            return
        }
        val totalMs = minutes * 60 * 1000L
        _playbackState.value = _playbackState.value.copy(sleepTimerRemainingMs = totalMs)

        sleepTimerJob = scope.launch {
            var remaining = totalMs
            while (remaining > 0L) {
                delay(1000L)
                remaining -= 1000L
                _playbackState.value = _playbackState.value.copy(sleepTimerRemainingMs = remaining)
                // Smooth fade-out during the final 10 seconds
                if (remaining in 1..10_000L) {
                    exoPlayer.volume = (remaining.toFloat() / 10_000f).coerceIn(0.05f, 1.0f)
                }
            }
            exoPlayer.pause()
            exoPlayer.volume = 1.0f
            _playbackState.value = _playbackState.value.copy(sleepTimerRemainingMs = 0L)
        }
    }

    fun updateCurrentMediaFavoriteStatus(isFavorite: Boolean) {
        val current = _playbackState.value.currentMedia ?: return
        val updated = current.copy(isFavorite = isFavorite)
        val updatedQueue = _playbackState.value.queue.map {
            if (it.id == current.id) updated else it
        }
        _playbackState.value = _playbackState.value.copy(
            currentMedia = updated,
            queue = updatedQueue
        )
    }

    fun closeMiniPlayer() {
        persistCurrentPosition()
        exoPlayer.stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isMiniPlayerVisible = false,
            isFullPlayerOpen = false
        )
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = exoPlayer.duration.takeIf { it > 0 }
                    ?: _playbackState.value.currentMedia?.durationMs
                    ?: 0L
                _playbackState.value = _playbackState.value.copy(
                    currentPositionMs = pos,
                    durationMs = dur
                )
                delay(500L)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
    }

    private fun buildFormatBadge(item: MediaItemModel): String {
        val ext = item.mimeType.substringAfterLast("/").uppercase().let {
            when (it) {
                "MPEG" -> "MP3"
                "MP4" -> if (item.mediaType.name == "AUDIO") "M4A" else "MP4"
                "X-WAV", "WAV" -> "WAV"
                "FLAC" -> "FLAC"
                else -> it.take(4)
            }
        }
        val durationSec = (item.durationMs / 1000L).coerceAtLeast(1L)
        val kbps = ((item.sizeBytes * 8) / 1000L / durationSec).coerceIn(128L, 1411L)
        val standardKbps = when {
            kbps >= 900 -> "${kbps}kbps"
            kbps >= 280 -> "320kbps"
            kbps >= 220 -> "256kbps"
            kbps >= 170 -> "192kbps"
            else -> "128kbps"
        }
        return "$ext  $standardKbps"
    }

    private fun loadLocalLyricsFor(item: MediaItemModel): List<LyricLine> {
        return runCatching {
            val folder = File(item.folderPath)
            val lrcFile = folder.listFiles()?.firstOrNull {
                it.extension.equals("lrc", ignoreCase = true) &&
                    it.nameWithoutExtension.contains(item.title.take(8), ignoreCase = true)
            }
            if (lrcFile != null && lrcFile.exists()) {
                parseLrcFile(lrcFile.readLines())
            } else {
                emptyList()
            }
        }.getOrDefault(emptyList())
    }

    private fun parseLrcFile(lines: List<String>): List<LyricLine> {
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")
        val parsed = mutableListOf<LyricLine>()
        for (line in lines) {
            val match = regex.find(line)
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val text = match.groupValues[4].trim()
                if (text.isNotEmpty()) {
                    parsed.add(LyricLine(timeMs = (min * 60 + sec) * 1000L, text = text))
                }
            }
        }
        return parsed
    }

    private fun startPlaybackServiceIfNeeded() {
        runCatching {
            val intent = Intent(context, PlayFlixPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private fun persistCurrentPosition() {
        val current = _playbackState.value.currentMedia ?: return
        val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
        recordHistory(current, pos)
    }

    private fun recordHistory(item: MediaItemModel, positionMs: Long) {
        scope.launch(Dispatchers.IO) {
            dao.upsertPlaybackHistory(
                PlaybackHistoryEntity(
                    mediaId = item.id,
                    title = item.title,
                    artist = item.artist,
                    contentUri = item.contentUri,
                    artworkUri = item.artworkUri,
                    mediaType = item.mediaType.name,
                    durationMs = item.durationMs,
                    lastPositionMs = positionMs
                )
            )
        }
    }
}
