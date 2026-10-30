#!/data/data/com.termux/files/usr/bin/bash
set -e

PROJECT_DIR="$HOME/PlayFlix"
echo "Creating PlayFlix project in $PROJECT_DIR..."
mkdir -p "$PROJECT_DIR"
cd "$PROJECT_DIR"

# Create directory tree
mkdir -p .github/workflows
mkdir -p gradle
mkdir -p app/src/main/java/com/playflix/player/core/common
mkdir -p app/src/main/java/com/playflix/player/core/design
mkdir -p app/src/main/java/com/playflix/player/core/model
mkdir -p app/src/main/java/com/playflix/player/core/database
mkdir -p app/src/main/java/com/playflix/player/core/media
mkdir -p app/src/main/java/com/playflix/player/core/permissions
mkdir -p app/src/main/java/com/playflix/player/navigation
mkdir -p app/src/main/java/com/playflix/player/feature/home
mkdir -p app/src/main/java/com/playflix/player/feature/library
mkdir -p app/src/main/java/com/playflix/player/feature/playlists
mkdir -p app/src/main/java/com/playflix/player/feature/settings
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/drawable

# ---------------------------------------------------------
# 1. .gitignore
# ---------------------------------------------------------
cat << 'EOF' > .gitignore
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
app/build
EOF

# ---------------------------------------------------------
# 2. GitHub Actions Workflow (.github/workflows/android-build.yml)
# ---------------------------------------------------------
cat << 'EOF' > .github/workflows/android-build.yml
name: PlayFlix Android CI

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build-apk:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: '8.7'

      - name: Build Debug APK
        run: gradle assembleDebug --stacktrace

      - name: Upload PlayFlix Debug APK
        uses: actions/upload-artifact@v4
        with:
          name: PlayFlix-Debug-APK
          path: app/build/outputs/apk/debug/app-debug.apk
EOF

# ---------------------------------------------------------
# 3. Gradle Configuration Files
# ---------------------------------------------------------
cat << 'EOF' > settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PlayFlix"
include(":app")
EOF

cat << 'EOF' > gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
EOF

cat << 'EOF' > gradle/libs.versions.toml
[versions]
agp = "8.4.2"
kotlin = "1.9.24"
ksp = "1.9.24-1.0.20"
coreKtx = "1.13.1"
lifecycle = "2.8.2"
activityCompose = "1.9.0"
composeBom = "2024.06.00"
navigationCompose = "2.7.7"
media3 = "1.3.1"
room = "2.6.1"
datastore = "1.1.1"
coil = "2.6.0"
coroutines = "1.8.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
androidx-media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }
androidx-media3-common = { group = "androidx.media3", name = "media3-common", version.ref = "media3" }

androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
coil-video = { group = "io.coil-kt", name = "coil-video", version.ref = "coil" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
jetbrains-kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
google-devtools-ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
EOF

cat << 'EOF' > build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
}
EOF

cat << 'EOF' > app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.playflix.player"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.playflix.player"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Media3 / ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Coil Image & Video Frame Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.video)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
EOF
cat << 'EOF' > app/proguard-rules.pro
-keep class com.playflix.player.core.model.** { *; }
-keep class androidx.media3.** { *; }
EOF

# ---------------------------------------------------------
# 4. AndroidManifest & Resources
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Offline Local Media Permissions (Android 8.0 to Android 14+) -->
    <uses-permission
        android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

    <!-- Background Playback Permissions -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:name=".PlayFlixApplication"
        android:allowBackup="true"
        android:icon="@android:drawable/ic_media_play"
        android:label="PlayFlix"
        android:roundIcon="@android:drawable/ic_media_play"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"
            android:exported="true"
            android:supportsPictureInPicture="true"
            android:theme="@android:style/Theme.Material.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
EOF

cat << 'EOF' > app/src/main/res/values/strings.xml
<resources>
    <string name="app_name">PlayFlix</string>
</resources>
EOF

# ---------------------------------------------------------
# 5. Core Common Utilities (Formatters & Greeting)
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/core/common/FormatUtils.kt
package com.playflix.player.core.common

import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object FormatUtils {

    fun getDynamicGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0L) return "00:00"
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMs)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 MB"
        val mb = sizeBytes.toDouble() / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            String.format(Locale.US, "%.2f GB", mb / 1024.0)
        } else {
            String.format(Locale.US, "%.1f MB", mb)
        }
    }
}
EOF
# ---------------------------------------------------------
# 6. Core Design System (Color, Typography, Theme, Components)
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/core/design/Color.kt
package com.playflix.player.core.design

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Dark Navy & Obsidian Palette (Inspired by Reference UI)
val PlayFlixNavyDark = Color(0xFF070A12)
val PlayFlixNavySurface = Color(0xFF101524)
val PlayFlixCardSurface = Color(0xFF161C2E)
val PlayFlixCardElevated = Color(0xFF1E263D)
val PlayFlixBorderDark = Color(0xFF26304D)

// Pure White Light Mode Palette
val PlayFlixLightBackground = Color(0xFFF8F9FC)
val PlayFlixLightSurface = Color(0xFFFFFFFF)
val PlayFlixLightCard = Color(0xFFF0F3F9)
val PlayFlixBorderLight = Color(0xFFE2E8F0)

// Accent Colors (Purple, Violet, Electric Blue, Emerald, Gold)
val PlayFlixViolet = Color(0xFF7C4DFF)
val PlayFlixPurple = Color(0xFF9D4EDD)
val PlayFlixMagenta = Color(0xFFD946EF)
val PlayFlixBlue = Color(0xFF00B4D8)
val PlayFlixTeal = Color(0xFF10B981)
val PlayFlixAmber = Color(0xFFF59E0B)
val PlayFlixCoral = Color(0xFFF43F5E)

// Quick Access Card Tints
val QuickMusicCardBg = Color(0xFF231942)
val QuickVideoCardBg = Color(0xFF12263A)
val QuickFolderCardBg = Color(0xFF0F292A)
val QuickFavCardBg = Color(0xFF2D2318)

// Text Colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)

object PlayFlixGradients {
    val PrimaryAccent = Brush.linearGradient(
        colors = listOf(PlayFlixViolet, PlayFlixPurple, PlayFlixMagenta)
    )

    val FeaturedBanner = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1D3557),
            Color(0xFF3A2D6A),
            Color(0xFF151B2D)
        )
    )

    val MiniPlayerSurface = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF181F34),
            Color(0xFF211C3A),
            Color(0xFF161C2E)
        )
    )
}
EOF

cat << 'EOF' > app/src/main/java/com/playflix/player/core/design/Type.kt
package com.playflix.player.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PlayFlixTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
EOF

cat << 'EOF' > app/src/main/java/com/playflix/player/core/design/Theme.kt
package com.playflix.player.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PlayFlixViolet,
    secondary = PlayFlixPurple,
    tertiary = PlayFlixBlue,
    background = PlayFlixNavyDark,
    surface = PlayFlixNavySurface,
    surfaceVariant = PlayFlixCardSurface,
    onPrimary = TextPrimaryDark,
    onSecondary = TextPrimaryDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = PlayFlixBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = PlayFlixViolet,
    secondary = PlayFlixPurple,
    tertiary = PlayFlixBlue,
    background = PlayFlixLightBackground,
    surface = PlayFlixLightSurface,
    surfaceVariant = PlayFlixLightCard,
    onPrimary = PlayFlixLightSurface,
    onSecondary = PlayFlixLightSurface,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = PlayFlixBorderLight
)

enum class AppThemeMode {
    DARK, LIGHT, SYSTEM
}

@Composable
fun PlayFlixTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (useDark) DarkColorScheme else LightColorScheme,
        typography = PlayFlixTypography,
        content = content
    )
}
EOF

cat << 'EOF' > app/src/main/java/com/playflix/player/core/design/Components.kt
package com.playflix.player.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String = "See All",
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onActionClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onActionClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = actionLabel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.onBackground
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
EOF
# ---------------------------------------------------------
# 7. Core Domain Models
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/core/model/MediaModels.kt
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
EOF

# ---------------------------------------------------------
# 8. Room Database (Favorites & Playback History / Resume)
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/core/database/PlayFlixDatabase.kt
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
EOF

# ---------------------------------------------------------
# 9. Local MediaStore Scanner (Audio, Video & Folders)
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/core/media/LocalMediaScanner.kt
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
EOF
# ---------------------------------------------------------
# 10. Media3 ExoPlayer Controller (Mini Player & Active Playback)
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/core/media/PlayFlixPlayerController.kt
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
EOF

# ---------------------------------------------------------
# 11. Runtime Permission Manager (Android 8.0 to 14+)
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/core/permissions/PermissionHelper.kt
package com.playflix.player.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun requiredMediaPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun hasMediaPermissions(context: Context): Boolean {
        return requiredMediaPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }
}
EOF

# ---------------------------------------------------------
# 12. HomeViewModel & Home Screen State
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/feature/home/HomeViewModel.kt
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
EOF
# ---------------------------------------------------------
# 13. Home Screen UI (Matching Reference Sketch 1 & Sketch 8)
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/feature/home/HomeScreen.kt
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
EOF
# ---------------------------------------------------------
# 14. Library, Playlist & More Screens (Connected to Real Data)
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/feature/library/LibraryScreen.kt
package com.playflix.player.feature.library

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.playflix.player.core.common.FormatUtils
import com.playflix.player.core.design.PlayFlixBlue
import com.playflix.player.core.design.PlayFlixCoral
import com.playflix.player.core.design.PlayFlixPurple
import com.playflix.player.core.model.MediaItemModel
import com.playflix.player.core.model.MediaType
import com.playflix.player.core.model.QuickAccessCategory
import com.playflix.player.feature.home.HomeUiState

@Composable
fun LibraryScreen(
    uiState: HomeUiState,
    initialFilter: QuickAccessCategory,
    onPlayMedia: (MediaItemModel, List<MediaItemModel>) -> Unit,
    onToggleFavorite: (MediaItemModel) -> Unit
) {
    var selectedCategory by remember(initialFilter) { mutableStateOf(initialFilter) }

    val displayedItems = when (selectedCategory) {
        QuickAccessCategory.MUSIC -> uiState.musicTracks
        QuickAccessCategory.VIDEOS -> uiState.videoTracks
        QuickAccessCategory.FAVORITES -> uiState.favoriteItems
        QuickAccessCategory.FOLDERS -> uiState.allMedia
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 14.dp)
    ) {
        Text(
            text = "Local Media Library",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAccessCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.name) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedItems, key = { "${it.mediaType}_${it.id}" }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onPlayMedia(item, displayedItems) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = item.artworkUri,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            imageVector = if (item.mediaType == MediaType.AUDIO) {
                                Icons.Default.MusicNote
                            } else {
                                Icons.Default.Videocam
                            },
                            contentDescription = null,
                            tint = if (item.mediaType == MediaType.AUDIO) PlayFlixPurple else PlayFlixBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.artist}  •  ${FormatUtils.formatDuration(item.durationMs)}  •  ${FormatUtils.formatFileSize(item.sizeBytes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(item) }) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) PlayFlixCoral else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }
}
EOF

# ---------------------------------------------------------
# 15. Navigation & Persistent Mini Player Overlay
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/navigation/PlayFlixNavigation.kt
package com.playflix.player.navigation

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
}

@Composable
private fun PersistentMiniPlayer(
    playbackState: ActivePlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    val media = playbackState.currentMedia ?: return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PlayFlixGradients.MiniPlayerSurface)
            .border(1.dp, PlayFlixViolet.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
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
EOF
# ---------------------------------------------------------
# 16. Application & MainActivity
# ---------------------------------------------------------
cat << 'EOF' > app/src/main/java/com/playflix/player/PlayFlixApplication.kt
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
EOF

cat << 'EOF' > app/src/main/java/com/playflix/player/MainActivity.kt
package com.playflix.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.playflix.player.core.design.AppThemeMode
import com.playflix.player.core.design.PlayFlixTheme
import com.playflix.player.core.permissions.PermissionHelper
import com.playflix.player.feature.home.HomeViewModel
import com.playflix.player.navigation.PlayFlixRootScreen

class MainActivity : ComponentActivity() {

    private val app: PlayFlixApplication
        get() = application as PlayFlixApplication

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(
            scanner = app.mediaScanner,
            dao = app.database.dao(),
            playerController = app.playerController
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(AppThemeMode.DARK) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionsMap ->
                val allGranted = permissionsMap.values.all { it }
                homeViewModel.onPermissionStatusChanged(allGranted)
            }

            LaunchedEffect(Unit) {
                val alreadyGranted = PermissionHelper.hasMediaPermissions(this@MainActivity)
                if (alreadyGranted) {
                    homeViewModel.onPermissionStatusChanged(true)
                } else {
                    permissionLauncher.launch(PermissionHelper.requiredMediaPermissions())
                }
            }

            PlayFlixTheme(themeMode = themeMode) {
                PlayFlixRootScreen(
                    viewModel = homeViewModel,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it },
                    onRequestPermissions = {
                        permissionLauncher.launch(PermissionHelper.requiredMediaPermissions())
                    }
                )
            }
        }
    }
}
EOF

echo "=================================================="
echo "Phase 1 files generated successfully in $PROJECT_DIR"
echo "=================================================="
