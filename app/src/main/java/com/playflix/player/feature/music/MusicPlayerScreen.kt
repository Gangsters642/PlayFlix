package com.playflix.player.feature.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.playflix.player.core.common.FormatUtils
import com.playflix.player.core.design.PlayFlixAmber
import com.playflix.player.core.design.PlayFlixCardElevated
import com.playflix.player.core.design.PlayFlixCardSurface
import com.playflix.player.core.design.PlayFlixGradients
import com.playflix.player.core.design.PlayFlixNavyDark
import com.playflix.player.core.design.PlayFlixPurple
import com.playflix.player.core.design.PlayFlixViolet
import com.playflix.player.core.media.ActivePlaybackState
import com.playflix.player.core.media.RepeatModeState
import com.playflix.player.core.model.MediaItemModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    playbackState: ActivePlaybackState,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (MediaItemModel) -> Unit,
    onSelectQueueIndex: (Int) -> Unit,
    onRemoveQueueIndex: (Int) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit
) {
    val currentMedia = playbackState.currentMedia ?: return

    var showQueueSheet by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1F1638),
            Color(0xFF0E1324),
            PlayFlixNavyDark
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Header Bar (Collapse, "Now Playing", Queue Button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse Player",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (playbackState.sleepTimerRemainingMs > 0L) {
                    Text(
                        text = "Sleep Timer: ${FormatUtils.formatDuration(playbackState.sleepTimerRemainingMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PlayFlixAmber
                    )
                }
            }

            IconButton(onClick = { showQueueSheet = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Open Queue",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Large Rounded Album Artwork Card (Sketch 2)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(PlayFlixCardSurface)
                .border(
                    width = 1.5.dp,
                    color = PlayFlixViolet.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = PlayFlixViolet.copy(alpha = 0.45f),
                modifier = Modifier.size(88.dp)
            )
            AsyncImage(
                model = currentMedia.artworkUri,
                contentDescription = currentMedia.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Song Title & Artist Name
        Text(
            text = currentMedia.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${currentMedia.artist}  •  ${currentMedia.album}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Format/Bitrate Pill & Favorite Pill Button (Sketch 2)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PlayFlixCardElevated)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = playbackState.formatBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (currentMedia.isFavorite) PlayFlixViolet.copy(alpha = 0.28f)
                        else PlayFlixCardElevated
                    )
                    .border(
                        width = 1.dp,
                        color = if (currentMedia.isFavorite) PlayFlixViolet else Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onToggleFavorite(currentMedia) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (currentMedia.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (currentMedia.isFavorite) PlayFlixAmber else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentMedia.isFavorite) "Favorited" else "Favorite",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Seek Bar + Current Time / Total Duration
        val totalDuration = playbackState.durationMs.coerceAtLeast(1L)
        val sliderPosition = playbackState.currentPositionMs.coerceIn(0L, totalDuration).toFloat()

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = sliderPosition,
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..totalDuration.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = PlayFlixViolet,
                    activeTrackColor = PlayFlixViolet,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = FormatUtils.formatDuration(playbackState.currentPositionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = FormatUtils.formatDuration(playbackState.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Main Circular Playback Controls Row (Shuffle, Prev, Play/Pause, Next, Repeat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playbackState.isShuffleEnabled) PlayFlixViolet else Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onSkipPrevious) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Large Circular Play/Pause Button with glowing border
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PlayFlixGradients.PrimaryAccent)
                    .border(3.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play or Pause",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = onSkipNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = onCycleRepeat) {
                val repeatIcon = when (playbackState.repeatMode) {
                    RepeatModeState.ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                val repeatTint = when (playbackState.repeatMode) {
                    RepeatModeState.OFF -> Color.White.copy(alpha = 0.65f)
                    else -> PlayFlixViolet
                }
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = "Repeat Mode",
                    tint = repeatTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 7. Utility Tools Row (Lyrics, Equalizer/Speed, Sleep Timer)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PlayerActionButton(
                icon = Icons.Default.Lyrics,
                label = "Lyrics",
                isActive = playbackState.lyrics.isNotEmpty(),
                onClick = { showLyricsSheet = true }
            )
            PlayerActionButton(
                icon = Icons.Default.Speed,
                label = "Speed (${playbackState.playbackSpeed}x)",
                isActive = playbackState.playbackSpeed != 1.0f,
                onClick = { showSpeedDialog = true }
            )
            PlayerActionButton(
                icon = Icons.Default.Timer,
                label = "Sleep Timer",
                isActive = playbackState.sleepTimerRemainingMs > 0L,
                onClick = { showSleepTimerDialog = true }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 8. Bottom "Next Up" Card (Sketch 2)
        playbackState.nextUpMedia?.let { nextTrack ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Next Up",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PlayFlixCardSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .clickable { onSkipNext() }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PlayFlixCardElevated)
                    ) {
                        AsyncImage(
                            model = nextTrack.artworkUri,
                            contentDescription = nextTrack.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nextTrack.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = nextTrack.artist,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Queue Options",
                            tint = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }

    // Queue Management Bottom Sheet
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            containerColor = PlayFlixCardSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Playing Queue (${playbackState.queue.size} tracks)",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(360.dp)
                ) {
                    itemsIndexed(playbackState.queue) { index, item ->
                        val isCurrent = index == playbackState.currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isCurrent) PlayFlixViolet.copy(alpha = 0.22f)
                                    else PlayFlixCardElevated
                                )
                                .clickable {
                                    onSelectQueueIndex(index)
                                    showQueueSheet = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isCurrent) PlayFlixViolet else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.artist} • ${FormatUtils.formatDuration(item.durationMs)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.65f),
                                    maxLines = 1
                                )
                            }
                            if (playbackState.queue.size > 1) {
                                IconButton(onClick = { onRemoveQueueIndex(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove from queue",
                                        tint = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Synchronized / Local Lyrics Bottom Sheet
    if (showLyricsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLyricsSheet = false },
            containerColor = PlayFlixCardSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lyrics — ${currentMedia.title}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(14.dp))
                if (playbackState.lyrics.isEmpty()) {
                    Text(
                        text = "No local .lrc lyric file found in ${currentMedia.folderName}.\nPlace a matching .lrc file alongside the song for synchronized offline lyrics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(340.dp)
                    ) {
                        itemsIndexed(playbackState.lyrics) { _, line ->
                            val isActiveLine = playbackState.currentPositionMs >= line.timeMs
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isActiveLine) PlayFlixViolet else Color.White.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeek(line.timeMs) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        val options = listOf(0 to "Off", 10 to "10 min", 15 to "15 min", 30 to "30 min", 60 to "60 min")
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            containerColor = PlayFlixCardSurface,
            title = { Text("Sleep Timer (Auto Fade-Out)", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Automatically fades out audio and pauses playback when the timer ends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.take(3).forEach { (mins, label) ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onSetSleepTimer(mins)
                                    showSleepTimerDialog = false
                                },
                                label = { Text(label, color = Color.White) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.drop(3).forEach { (mins, label) ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onSetSleepTimer(mins)
                                    showSleepTimerDialog = false
                                },
                                label = { Text(label, color = Color.White) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Close", color = PlayFlixViolet)
                }
            }
        )
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = PlayFlixCardSurface,
            title = { Text("Playback Speed", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        speeds.take(3).forEach { spd ->
                            FilterChip(
                                selected = playbackState.playbackSpeed == spd,
                                onClick = {
                                    onSetPlaybackSpeed(spd)
                                    showSpeedDialog = false
                                },
                                label = { Text("${spd}x", color = Color.White) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        speeds.drop(3).forEach { spd ->
                            FilterChip(
                                selected = playbackState.playbackSpeed == spd,
                                onClick = {
                                    onSetPlaybackSpeed(spd)
                                    showSpeedDialog = false
                                },
                                label = { Text("${spd}x", color = Color.White) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = PlayFlixViolet)
                }
            }
        )
    }
}

@Composable
private fun PlayerActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) PlayFlixViolet else Color.White.copy(alpha = 0.75f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) PlayFlixViolet else Color.White.copy(alpha = 0.75f)
        )
    }
}
