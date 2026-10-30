package com.playflix.player.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.playflix.player.core.common.FormatUtils
import com.playflix.player.core.design.CircularIconButton
import com.playflix.player.core.design.PlayFlixAmber
import com.playflix.player.core.design.PlayFlixBlue
import com.playflix.player.core.design.PlayFlixGradients
import com.playflix.player.core.design.PlayFlixPurple
import com.playflix.player.core.design.PlayFlixTeal
import com.playflix.player.core.design.PlayFlixViolet
import com.playflix.player.core.design.QuickFavCardBg
import com.playflix.player.core.design.QuickFolderCardBg
import com.playflix.player.core.design.QuickMusicCardBg
import com.playflix.player.core.design.QuickVideoCardBg
import com.playflix.player.core.design.SectionHeader
import com.playflix.player.core.model.MediaFolderModel
import com.playflix.player.core.model.MediaItemModel
import com.playflix.player.core.model.MediaType
import com.playflix.player.core.model.QuickAccessCategory

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRequestPermissions: () -> Unit,
    onQuickAccessClick: (QuickAccessCategory) -> Unit,
    onPlayMedia: (MediaItemModel, List<MediaItemModel>) -> Unit,
    onFolderClick: (MediaFolderModel) -> Unit,
    onSeeAllRecent: () -> Unit,
    onSeeAllFolders: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val greeting = remember { FormatUtils.getDynamicGreeting() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // 1. Top Branding & Greeting Bar
        item {
            PlayFlixTopAppBar(
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick
            )
        }

        // 2. Featured Greeting Banner
        item {
            FeaturedBannerCard(
                greeting = greeting,
                featuredItem = uiState.featuredItem,
                onQuickPlay = {
                    val target = uiState.featuredItem ?: uiState.allMedia.firstOrNull()
                    if (target != null) {
                        onPlayMedia(target, uiState.allMedia)
                    } else if (!uiState.hasPermissions) {
                        onRequestPermissions()
                    }
                }
            )
        }

        // 3. 4 Quick Access Cards (Music, Videos, Folders, Favorites)
        item {
            QuickAccessRow(
                musicCount = uiState.musicTracks.size,
                videoCount = uiState.videoTracks.size,
                folderCount = uiState.folders.size,
                favCount = uiState.favoriteItems.size,
                onCategoryClick = onQuickAccessClick
            )
        }

        // Permission or Loading State
        if (!uiState.hasPermissions) {
            item {
                PermissionPromptCard(onRequestPermissions = onRequestPermissions)
            }
        } else if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PlayFlixViolet)
                }
            }
        } else {
            // 4. Continue Playing Section (when history exists)
            uiState.continuePlayingItem?.let { resumeItem ->
                item {
                    ContinuePlayingCard(
                        item = resumeItem,
                        onResume = { onPlayMedia(resumeItem, uiState.allMedia) }
                    )
                }
            }

            // 5. Recently Played Horizontal Carousel
            item {
                SectionHeader(
                    title = "Recently Played",
                    actionLabel = "See All",
                    onActionClick = onSeeAllRecent
                )
                if (uiState.recentlyPlayed.isEmpty()) {
                    EmptyMediaBanner(onRescan = onRefreshClick)
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.recentlyPlayed, key = { "${it.mediaType}_${it.id}" }) { media ->
                            RecentMediaCard(
                                item = media,
                                onClick = { onPlayMedia(media, uiState.recentlyPlayed) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 6. Folders Section (My Music, My Videos, Downloads, Discovered)
            item {
                SectionHeader(
                    title = "Folders",
                    actionLabel = "See All",
                    onActionClick = onSeeAllFolders
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.folders, key = { it.path }) { folder ->
                        FolderDashboardCard(
                            folder = folder,
                            onClick = { onFolderClick(folder) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayFlixTopAppBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PlayFlixGradients.PrimaryAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "PlayFlix Logo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PlayFlix",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "👑",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "Music  •  Videos  •  Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularIconButton(
                icon = Icons.Default.Search,
                contentDescription = "Search Media",
                onClick = onSearchClick
            )
            CircularIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun FeaturedBannerCard(
    greeting: String,
    featuredItem: MediaItemModel?,
    onQuickPlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .height(148.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(PlayFlixGradients.FeaturedBanner)
            .border(
                width = 1.dp,
                color = PlayFlixViolet.copy(alpha = 0.35f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onQuickPlay() }
    ) {
        if (featuredItem?.artworkUri != null) {
            AsyncImage(
                model = featuredItem.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.25f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xCC0D1326),
                            Color(0x881A1F3C),
                            Color(0x331A1F3C)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = featuredItem?.let { "Up Next: ${it.title}" }
                        ?: "Great Music Makes\nBetter Days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onQuickPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Quick Play",
                    tint = PlayFlixViolet,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickAccessRow(
    musicCount: Int,
    videoCount: Int,
    folderCount: Int,
    favCount: Int,
    onCategoryClick: (QuickAccessCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickAccessItem(
            modifier = Modifier.weight(1f),
            title = "Music",
            countLabel = "$musicCount",
            icon = Icons.Default.MusicNote,
            accentColor = PlayFlixPurple,
            cardBackground = QuickMusicCardBg,
            onClick = { onCategoryClick(QuickAccessCategory.MUSIC) }
        )
        QuickAccessItem(
            modifier = Modifier.weight(1f),
            title = "Videos",
            countLabel = "$videoCount",
            icon = Icons.Default.Videocam,
            accentColor = PlayFlixBlue,
            cardBackground = QuickVideoCardBg,
            onClick = { onCategoryClick(QuickAccessCategory.VIDEOS) }
        )
        QuickAccessItem(
            modifier = Modifier.weight(1f),
            title = "Folders",
            countLabel = "$folderCount",
            icon = Icons.Default.Folder,
            accentColor = PlayFlixTeal,
            cardBackground = QuickFolderCardBg,
            onClick = { onCategoryClick(QuickAccessCategory.FOLDERS) }
        )
        QuickAccessItem(
            modifier = Modifier.weight(1f),
            title = "Favorites",
            countLabel = "$favCount",
            icon = Icons.Default.Star,
            accentColor = PlayFlixAmber,
            cardBackground = QuickFavCardBg,
            onClick = { onCategoryClick(QuickAccessCategory.FAVORITES) }
        )
    }
}

@Composable
private fun QuickAccessItem(
    modifier: Modifier,
    title: String,
    countLabel: String,
    icon: ImageVector,
    accentColor: Color,
    cardBackground: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cardBackground)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = countLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun ContinuePlayingCard(
    item: MediaItemModel,
    onResume: () -> Unit
) {
    val progress = if (item.durationMs > 0) {
        (item.lastPositionMs.toFloat() / item.durationMs.toFloat()).coerceIn(0.05f, 1f)
    } else 0.1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onResume() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PlayFlixViolet.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.artworkUri,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CONTINUE PLAYING",
                    style = MaterialTheme.typography.labelSmall,
                    color = PlayFlixViolet
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.artist} • ${FormatUtils.formatDuration(item.lastPositionMs)} / ${FormatUtils.formatDuration(item.durationMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PlayFlixViolet),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = PlayFlixViolet,
            trackColor = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun RecentMediaCard(
    item: MediaItemModel,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(122.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(122.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = item.artworkUri,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Fallback center icon when no artwork exists
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.mediaType == MediaType.AUDIO) {
                        Icons.Default.MusicNote
                    } else {
                        Icons.Default.Videocam
                    },
                    contentDescription = item.mediaType.name,
                    tint = if (item.mediaType == MediaType.AUDIO) PlayFlixPurple else PlayFlixBlue,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = item.artist,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FolderDashboardCard(
    folder: MediaFolderModel,
    onClick: () -> Unit
) {
    val folderColor = when (folder.name) {
        "My Music" -> PlayFlixPurple
        "My Videos" -> PlayFlixBlue
        "Downloads" -> PlayFlixTeal
        else -> PlayFlixViolet
    }

    Column(
        modifier = Modifier
            .width(125.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = folder.name,
            tint = folderColor,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = folder.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = folder.subtitleLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun PermissionPromptCard(onRequestPermissions: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, PlayFlixViolet.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Offline Media Access Required",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "PlayFlix works 100% offline. Grant local Audio & Video permission so PlayFlix can discover songs, videos, and folders stored on your Android device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequestPermissions,
            colors = ButtonDefaults.buttonColors(containerColor = PlayFlixViolet)
        ) {
            Text(text = "Grant Media Permission", color = Color.White)
        }
    }
}

@Composable
private fun EmptyMediaBanner(onRescan: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "No Local Media Found Yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Add MP3/MP4 files to your device or tap Rescan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CircularIconButton(
            icon = Icons.Default.Refresh,
            contentDescription = "Rescan Library",
            onClick = onRescan
        )
    }
}
