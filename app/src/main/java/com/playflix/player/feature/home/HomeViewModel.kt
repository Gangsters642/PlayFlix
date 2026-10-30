package com.playflix.player.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.playflix.player.core.database.FavoriteEntity
import com.playflix.player.core.database.PlayFlixDao
import com.playflix.player.core.media.LocalMediaScanner
import com.playflix.player.core.media.PlayFlixPlayerController
import com.playflix.player.core.model.MediaFolderModel
import com.playflix.player.core.model.MediaItemModel
import com.playflix.player.core.model.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val hasPermissions: Boolean = false,
    val allMedia: List<MediaItemModel> = emptyList(),
    val musicTracks: List<MediaItemModel> = emptyList(),
    val videoTracks: List<MediaItemModel> = emptyList(),
    val favoriteItems: List<MediaItemModel> = emptyList(),
    val recentlyPlayed: List<MediaItemModel> = emptyList(),
    val continuePlayingItem: MediaItemModel? = null,
    val folders: List<MediaFolderModel> = emptyList(),
    val featuredItem: MediaItemModel? = null
)

class HomeViewModel(
    private val scanner: LocalMediaScanner,
    private val dao: PlayFlixDao,
    val playerController: PlayFlixPlayerController
) : ViewModel() {

    private val _rawMedia = MutableStateFlow<List<MediaItemModel>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _hasPermissions = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        _rawMedia,
        _isLoading,
        _hasPermissions,
        dao.observeFavorites(),
        dao.observeRecentHistory()
    ) { mediaList, loading, granted, favorites, history ->
        val favIds = favorites.map { it.mediaId }.toSet()
        val historyMap = history.associateBy { it.mediaId }

        val enrichedMedia = mediaList.map { item ->
            item.copy(
                isFavorite = item.id in favIds,
                lastPositionMs = historyMap[item.id]?.lastPositionMs ?: 0L
            )
        }

        val music = enrichedMedia.filter { it.mediaType == MediaType.AUDIO }
        val videos = enrichedMedia.filter { it.mediaType == MediaType.VIDEO }
        val favs = enrichedMedia.filter { it.isFavorite }

        val recentItems = history.mapNotNull { hist ->
            enrichedMedia.find { it.id == hist.mediaId }
        }.ifEmpty {
            enrichedMedia.take(10)
        }

        val continueItem = history.firstOrNull()?.let { latest ->
            enrichedMedia.find { it.id == latest.mediaId }
        }

        HomeUiState(
            isLoading = loading,
            hasPermissions = granted,
            allMedia = enrichedMedia,
            musicTracks = music,
            videoTracks = videos,
            favoriteItems = favs,
            recentlyPlayed = recentItems,
            continuePlayingItem = continueItem,
            folders = scanner.buildSmartFolders(enrichedMedia),
            featuredItem = continueItem ?: recentItems.firstOrNull() ?: enrichedMedia.firstOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onPermissionStatusChanged(granted: Boolean) {
        _hasPermissions.value = granted
        if (granted) {
            refreshLibrary()
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            val scanned = scanner.scanAllMedia()
            _rawMedia.value = scanned
            _isLoading.value = false
        }
    }

    fun toggleFavorite(item: MediaItemModel) {
        viewModelScope.launch {
            if (item.isFavorite) {
                dao.removeFavorite(item.id)
            } else {
                dao.insertFavorite(
                    FavoriteEntity(
                        mediaId = item.id,
                        mediaType = item.mediaType.name
                    )
                )
            }
        }
    }

    class Factory(
        private val scanner: LocalMediaScanner,
        private val dao: PlayFlixDao,
        private val playerController: PlayFlixPlayerController
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(scanner, dao, playerController) as T
        }
    }
}
