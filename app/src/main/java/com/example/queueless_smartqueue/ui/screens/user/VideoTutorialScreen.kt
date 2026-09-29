package com.example.queueless_smartqueue.ui.screens.user

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.theme.*
import com.example.queueless_smartqueue.util.VideoTutorialManager

data class VideoTopic(
    val title: String,
    val description: String,
    val icon: String
)

@OptIn(UnstableApi::class)
@Composable
fun VideoTutorialScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Obtain active video URI from VideoTutorialManager (bundled raw video or admin-uploaded video)
    val videoUri = remember {
        VideoTutorialManager.getVideoUri(context)
    }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
        }
    }

    LaunchedEffect(videoUri) {
        videoUri?.let { uri ->
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    val topics = listOf(
        VideoTopic(
            title = "Welcome to QueueLess",
            description = "Overview of how the app works and eliminates physical waiting lines",
            icon = "👋"
        ),
        VideoTopic(
            title = "Account Registration & Sign Up",
            description = "Quick and secure account setup with name, phone number, and email",
            icon = "👤"
        ),
        VideoTopic(
            title = "Select Department & Counter",
            description = "Browse available organizations and view live open counters",
            icon = "🏢"
        ),
        VideoTopic(
            title = "Check Wait Time & Book Token",
            description = "See real-time people ahead, estimated wait time, and generate instant digital pass",
            icon = "🎫"
        ),
        VideoTopic(
            title = "Live Real-Time Queue Tracking",
            description = "Watch your live position advance with automatic turn alerts",
            icon = "📊"
        ),
        VideoTopic(
            title = "Profile, SMS Alerts & Settings",
            description = "Customize notification preferences, SMS triggers, and access Help & Support",
            icon = "⚙️"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "App Video Guide",
            subtitle = "Learn how QueueLess works in 1 minute",
            showBackButton = true,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
        ) {
            // Video Player Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    if (videoUri != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                    setShowNextButton(false)
                                    setShowPreviousButton(false)
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Empty state if video is not available yet
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.OndemandVideo,
                                contentDescription = null,
                                tint = ActionBlue,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tutorial Video Not Available",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = CardWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Admin can upload a video from the Staff Admin Console.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CardWhite.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }

            // Topics Headline Section
            item {
                Text(
                    text = "What's in this video",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            // Informational Topic Cards (Headlines & descriptions only, no timeline/timestamps)
            items(topics) { topic ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ChipBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = topic.icon, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = topic.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            )
                        }
                    }
                }
            }

            // Multimedia Architecture Card for Viva / Evaluation
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ChipBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ActionBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Multimedia Architecture",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Powered by AndroidX Media3 (ExoPlayer) with hardware-accelerated H.264 video decoding, dynamic aspect-ratio playback, and offline local resource streaming.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
