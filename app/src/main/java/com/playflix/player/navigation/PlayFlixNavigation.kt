package com.playflix.player.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.playflix.player.core.design.AppThemeMode
import com.playflix.player.core.design.PlayFlixGradients
import com.playflix.player.core.design.PlayFlixViolet
import com.playflix.player.core.media.ActivePlaybackState
import com.playflix.player.core.model.QuickAccessCategory
import com.playflix.player.feature.home.HomeScreen
import com.playflix.player.feature.home.HomeViewModel
import com.playflix.player.feature.library.LibraryScreen
import com.playflix.player.feature.music.MusicPlayerScreen

enum class BottomNavTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    PLAYLIST("Playlist", Icons.AutoMirrored.Filled.QueueMusic),
    MORE("More", Icons.Default.MoreHoriz)
}

@Composable
fun PlayFlixRootScreen(
    viewModel: HomeViewModel,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onRequestPermissions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val playbackState by viewModel.playerController.playbackState.collectAsState()

    var currentTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var selectedLibraryCategory by remember { mutableStateOf(QuickAccessCategory.MUSIC) }

    BackHandler(enabled = playbackState.isFullPlayerOpen) {
        viewModel.playerController.closeFullPlayer()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (playbackState.isMiniPlayerVisible && playbackState.currentMedia != null) {
                        PersistentMiniPlayer(
                            playbackState = playbackState,
                            onOpenFullPlayer = { viewModel.playerController.openFullPlayer() },
                            onPlayPause = { viewModel.playerController.togglePlayPause() },
                            onNext = { viewModel.playerController.skipNext() },
                            onClose = { viewModel.playerController.closeMiniPlayer() }
                        )
                    }

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        BottomNavTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PlayFlixViolet,
                                    selectedTextColor = PlayFlixViolet,
                                    indicatorColor = PlayFlixViolet.copy(alpha = 0.16f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    BottomNavTab.HOME -> HomeScreen(
                        uiState = uiState,
                        onRequestPermissions = onRequestPermissions,
                        onQuickAccessClick = { category ->
                            selectedLibraryCategory = category
                            currentTab = BottomNavTab.LIBRARY
                        },
                        onPlayMedia = { item, queue ->
                            viewModel.playerController.playMedia(item, queue)
                        },
                        onFolderClick = {
                            selectedLibraryCategory = QuickAccessCategory.FOLDERS
                            currentTab = BottomNavTab.LIBRARY
                        },
                        onSeeAllRecent = {
                            selectedLibraryCategory = QuickAccessCategory.MUSIC
                            currentTab = BottomNavTab.LIBRARY
                        },
                        onSeeAllFolders = {
                            selectedLibraryCategory = QuickAccessCategory.FOLDERS
                            currentTab = BottomNavTab.LIBRARY
                        },
                        onSearchClick = { currentTab = BottomNavTab.LIBRARY },
                        onSettingsClick = { currentTab = BottomNavTab.MORE },
                        onRefreshClick = { viewModel.refreshLibrary() }
                    )

                    BottomNavTab.LIBRARY -> LibraryScreen(
                        uiState = uiState,
                        initialFilter = selectedLibraryCategory,
                        onPlayMedia = { item, queue ->
                            viewModel.playerController.playMedia(item, queue)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )

                    BottomNavTab.PLAYLIST -> LibraryScreen(
                        uiState = uiState,
                        initialFilter = QuickAccessCategory.FAVORITES,
                        onPlayMedia = { item, queue ->
                            viewModel.playerController.playMedia(item, queue)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )

                    BottomNavTab.MORE -> MoreHubScreen(
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        totalSongs = uiState.musicTracks.size,
                        totalVideos = uiState.videoTracks.size
                    )
                }
            }
        }

        // Full-Screen Music Player Overlay (Slides up when Mini Player is tapped)
        AnimatedVisibility(
            visible = playbackState.isFullPlayerOpen && playbackState.currentMedia != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            MusicPlayerScreen(
                playbackState = playbackState,
                onCollapse = { viewModel.playerController.closeFullPlayer() },
                onPlayPause = { viewModel.playerController.togglePlayPause() },
                onSeek = { viewModel.playerController.seekTo(it) },
                onSkipNext = { viewModel.playerController.skipNext() },
                onSkipPrevious = { viewModel.playerController.skipPrevious() },
                onToggleShuffle = { viewModel.playerController.toggleShuffle() },
                onCycleRepeat = { viewModel.playerController.cycleRepeatMode() },
                onToggleFavorite = { media ->
                    viewModel.toggleFavorite(media)
                    viewModel.playerController.updateCurrentMediaFavoriteStatus(!media.isFavorite)
                },
                onSelectQueueIndex = { index ->
                    viewModel.playerController.playFromQueueIndex(index)
                },
                onRemoveQueueIndex = { index ->
                    viewModel.playerController.removeFromQueue(index)
                },
                onSetSleepTimer = { mins ->
                    viewModel.playerController.setSleepTimer(mins)
                },
                onSetPlaybackSpeed = { speed ->
                    viewModel.playerController.setPlaybackSpeed(speed)
                }
            )
        }
    }
}

@Composable
private fun PersistentMiniPlayer(
    playbackState: ActivePlaybackState,
    onOpenFullPlayer: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    val media = playbackState.currentMedia ?: return
    val progress = if (playbackState.durationMs > 0) {
        (playbackState.currentPositionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PlayFlixGradients.MiniPlayerSurface)
            .border(1.dp, PlayFlixViolet.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable { onOpenFullPlayer() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PlayFlixViolet.copy(alpha = 0.25f))
            ) {
                AsyncImage(
                    model = media.artworkUri,
                    contentDescription = media.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = media.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PlayFlixViolet)
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play or Pause",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = Color.White
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Mini Player",
                    tint = Color.White.copy(alpha = 0.75f)
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = PlayFlixViolet,
            trackColor = Color.Transparent
        )
    }
}

@Composable
private fun MoreHubScreen(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    totalSongs: Int,
    totalVideos: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "PlayFlix 👑 Settings & More",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pure White Light Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Switch between Dark Navy and Pure White",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = themeMode == AppThemeMode.LIGHT,
                onCheckedChange = { isLight ->
                    onThemeModeChange(if (isLight) AppThemeMode.LIGHT else AppThemeMode.DARK)
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(
                text = "Offline Library Status",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Discovered Songs: $totalSongs  •  Discovered Videos: $totalVideos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
