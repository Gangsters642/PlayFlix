package com.playflix.player.core.model

enum class MediaType {
    AUDIO, VIDEO
}

data class MediaItemModel(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val contentUri: String,
    val artworkUri: String?,
    val folderName: String,
    val folderPath: String,
    val mimeType: String,
    val mediaType: MediaType,
    val isFavorite: Boolean = false,
    val lastPositionMs: Long = 0L
)

data class MediaFolderModel(
    val name: String,
    val path: String,
    val audioCount: Int,
    val videoCount: Int
) {
    val totalCount: Int get() = audioCount + videoCount

    val subtitleLabel: String
        get() = when {
            audioCount > 0 && videoCount == 0 -> "$audioCount songs"
            videoCount > 0 && audioCount == 0 -> "$videoCount videos"
            else -> "$totalCount files"
        }
}

enum class QuickAccessCategory {
    MUSIC, VIDEOS, FOLDERS, FAVORITES
}
