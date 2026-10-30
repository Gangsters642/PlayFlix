package com.playflix.player.core.media

import android.content.Context
import android.net.Uri
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivePlaybackState(
    val currentMedia: MediaItemModel? = null,
    val queue: List<MediaItemModel> = emptyList(),
    val isPlaying: Boolean = false,
    val isMiniPlayerVisible: Boolean = false
)

class PlayFlixPlayerController(
    context: Context,
    private val dao: PlayFlixDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                persistCurrentPosition()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                val queue = _playbackState.value.queue
                if (index in queue.indices) {
                    val nextTrack = queue[index]
                    _playbackState.value = _playbackState.value.copy(
                        currentMedia = nextTrack,
                        isMiniPlayerVisible = true
                    )
                    recordHistory(nextTrack, 0L)
                }
            }
        })
    }

    fun playMedia(item: MediaItemModel, queueList: List<MediaItemModel> = listOf(item)) {
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
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, startIndex, item.lastPositionMs)
        exoPlayer.prepare()
        exoPlayer.play()

        _playbackState.value = ActivePlaybackState(
            currentMedia = item,
            queue = targetQueue,
            isPlaying = true,
            isMiniPlayerVisible = true
        )
        recordHistory(item, item.lastPositionMs)
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun skipNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_playbackState.value.queue.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
            exoPlayer.play()
        }
    }

    fun closeMiniPlayer() {
        persistCurrentPosition()
        exoPlayer.stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isMiniPlayerVisible = false
        )
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
