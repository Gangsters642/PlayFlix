package com.playflix.player.core.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val mediaId: Long,
    val mediaType: String,
    val addedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey val mediaId: Long,
    val title: String,
    val artist: String,
    val contentUri: String,
    val artworkUri: String?,
    val mediaType: String,
    val durationMs: Long,
    val lastPositionMs: Long,
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Dao
interface PlayFlixDao {
    @Query("SELECT * FROM favorites ORDER BY addedAtMs DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE mediaId = :mediaId")
    suspend fun removeFavorite(mediaId: Long)

    @Query("SELECT * FROM playback_history ORDER BY updatedAtMs DESC LIMIT 20")
    fun observeRecentHistory(): Flow<List<PlaybackHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaybackHistory(entity: PlaybackHistoryEntity)
}

@Database(
    entities = [FavoriteEntity::class, PlaybackHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PlayFlixDatabase : RoomDatabase() {
    abstract fun dao(): PlayFlixDao

    companion object {
        @Volatile
        private var INSTANCE: PlayFlixDatabase? = null

        fun getInstance(context: Context): PlayFlixDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlayFlixDatabase::class.java,
                    "playflix_offline.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
