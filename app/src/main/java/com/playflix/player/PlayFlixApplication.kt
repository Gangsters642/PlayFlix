package com.playflix.player

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.playflix.player.core.database.PlayFlixDatabase
import com.playflix.player.core.media.LocalMediaScanner
import com.playflix.player.core.media.PlayFlixPlayerController

class PlayFlixApplication : Application(), ImageLoaderFactory {

    val database by lazy { PlayFlixDatabase.getInstance(this) }
    val mediaScanner by lazy { LocalMediaScanner(this) }
    val playerController by lazy { PlayFlixPlayerController(this, database.dao()) }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
