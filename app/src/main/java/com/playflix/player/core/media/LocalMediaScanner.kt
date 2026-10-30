package com.playflix.player.core.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.playflix.player.core.model.MediaFolderModel
import com.playflix.player.core.model.MediaItemModel
import com.playflix.player.core.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalMediaScanner(private val context: Context) {

    suspend fun scanAllMedia(): List<MediaItemModel> = withContext(Dispatchers.IO) {
        val allItems = mutableListOf<MediaItemModel>()
        allItems.addAll(scanAudioFiles())
        allItems.addAll(scanVideoFiles())
        allItems.sortedByDescending { it.dateAddedSec }
    }

    private fun scanAudioFiles(): List<MediaItemModel> {
        val audioList = mutableListOf<MediaItemModel>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 3000"

        runCatching {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val rawTitle = cursor.getString(titleCol) ?: "Unknown Track"
                    val rawArtist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val artist = if (rawArtist == "<unknown>") "Unknown Artist" else rawArtist
                    val rawAlbum = cursor.getString(albumCol) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdCol)
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val dateAdded = cursor.getLong(dateCol)
                    val filePath = cursor.getString(dataCol) ?: ""
                    val mimeType = cursor.getString(mimeCol) ?: "audio/*"

                    val parentFile = File(filePath).parentFile
                    val folderName = parentFile?.name ?: "My Music"
                    val folderPath = parentFile?.absolutePath ?: "/storage/emulated/0/Music"

                    val contentUri = ContentUris.withAppendedId(collection, id).toString()
                    val artworkUri = ContentUris.withAppendedId(albumArtBaseUri, albumId).toString()

                    audioList.add(
                        MediaItemModel(
                            id = id,
                            title = rawTitle,
                            artist = artist,
                            album = rawAlbum,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAddedSec = dateAdded,
                            contentUri = contentUri,
                            artworkUri = artworkUri,
                            folderName = folderName,
                            folderPath = folderPath,
                            mimeType = mimeType,
                            mediaType = MediaType.AUDIO
                        )
                    )
                }
            }
        }
        return audioList
    }

    private fun scanVideoFiles(): List<MediaItemModel> {
        val videoList = mutableListOf<MediaItemModel>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MIME_TYPE
        )
        val selection = "${MediaStore.Video.Media.DURATION} >= 1000"

        runCatching {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Video"
                    val rawArtist = cursor.getString(artistCol)
                    val artist = if (rawArtist.isNullOrBlank() || rawArtist == "<unknown>") "Local Video" else rawArtist
                    val album = cursor.getString(albumCol) ?: "Videos"
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val dateAdded = cursor.getLong(dateCol)
                    val filePath = cursor.getString(dataCol) ?: ""
                    val mimeType = cursor.getString(mimeCol) ?: "video/*"

                    val parentFile = File(filePath).parentFile
                    val folderName = parentFile?.name ?: "My Videos"
                    val folderPath = parentFile?.absolutePath ?: "/storage/emulated/0/Movies"

                    val contentUri = ContentUris.withAppendedId(collection, id).toString()

                    videoList.add(
                        MediaItemModel(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAddedSec = dateAdded,
                            contentUri = contentUri,
                            artworkUri = contentUri,
                            folderName = folderName,
                            folderPath = folderPath,
                            mimeType = mimeType,
                            mediaType = MediaType.VIDEO
                        )
                    )
                }
            }
        }
        return videoList
    }

    fun buildSmartFolders(mediaItems: List<MediaItemModel>): List<MediaFolderModel> {
        val audioCount = mediaItems.count { it.mediaType == MediaType.AUDIO }
        val videoCount = mediaItems.count { it.mediaType == MediaType.VIDEO }
        val downloadItems = mediaItems.filter {
            it.folderName.equals("Download", ignoreCase = true) ||
                it.folderName.equals("Downloads", ignoreCase = true)
        }

        val primaryCards = mutableListOf(
            MediaFolderModel(
                name = "My Music",
                path = "virtual://my_music",
                audioCount = audioCount,
                videoCount = 0
            ),
            MediaFolderModel(
                name = "My Videos",
                path = "virtual://my_videos",
                audioCount = 0,
                videoCount = videoCount
            ),
            MediaFolderModel(
                name = "Downloads",
                path = "virtual://downloads",
                audioCount = downloadItems.count { it.mediaType == MediaType.AUDIO },
                videoCount = downloadItems.count { it.mediaType == MediaType.VIDEO }
            )
        )

        val discovered = mediaItems
            .groupBy { it.folderPath }
            .map { (path, list) ->
                MediaFolderModel(
                    name = list.first().folderName,
                    path = path,
                    audioCount = list.count { it.mediaType == MediaType.AUDIO },
                    videoCount = list.count { it.mediaType == MediaType.VIDEO }
                )
            }
            .filter {
                !it.name.equals("Download", ignoreCase = true) &&
                    !it.name.equals("Downloads", ignoreCase = true)
            }
            .sortedByDescending { it.totalCount }

        return primaryCards + discovered
    }
}
