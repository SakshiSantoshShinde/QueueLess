package com.example.queueless_smartqueue.ui.screens.staff

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Restore
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
import com.example.queueless_smartqueue.ui.components.PrimaryButton
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.components.SecondaryButton
import com.example.queueless_smartqueue.ui.components.StatusBadge
import com.example.queueless_smartqueue.ui.theme.*
import com.example.queueless_smartqueue.util.VideoTutorialManager

@OptIn(UnstableApi::class)
@Composable
fun AdminVideoManagementScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var isCustom by remember {
        mutableStateOf(VideoTutorialManager.isCustomVideoUploaded(context))
    }

    var videoUri by remember {
        mutableStateOf(VideoTutorialManager.getVideoUri(context))
    }

    // Video Picker launcher from storage
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val success = VideoTutorialManager.saveCustomVideo(context, uri)
            if (success) {
                isCustom = true
                videoUri = VideoTutorialManager.getVideoUri(context)
                Toast.makeText(context, "Tutorial video updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save video. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Video Management",
            subtitle = "Admin Console • Tutorial Walkthrough",
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
            // Live Status Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCustom) {
                        StatusBadge(
                            text = "🟣 Custom Admin Video Active",
                            backgroundColor = ChipBackground,
                            textColor = ActionBlue
                        )
                    } else {
                        StatusBadge(
                            text = "🟢 Bundled Default Video Active",
                            backgroundColor = StatusBackgroundGreen,
                            textColor = SuccessGreen
                        )
                    }

                    Text(
                        text = "User Visible",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            // Video Player Preview Card
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
                                text = "No Video Currently Loaded",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = CardWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Admin Upload & Control Actions
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Admin Video Controls",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Text(
                            text = "Upload a video from your phone storage to update what regular users see in the 'How to Use QueueLess' guide screen.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        )

                        PrimaryButton(
                            text = "Upload New Video",
                            onClick = { videoPickerLauncher.launch("video/*") },
                            icon = Icons.Default.CloudUpload,
                            backgroundColor = ActionBlue,
                            modifier = Modifier.height(48.dp)
                        )

                        if (isCustom) {
                            SecondaryButton(
                                text = "Reset to Bundled Video",
                                onClick = {
                                    VideoTutorialManager.resetToDefaultVideo(context)
                                    isCustom = false
                                    videoUri = VideoTutorialManager.getVideoUri(context)
                                    Toast.makeText(context, "Reverted to default bundled video", Toast.LENGTH_SHORT).show()
                                },
                                icon = Icons.Outlined.Restore,
                                borderColor = WarningAmber,
                                textColor = WarningAmber,
                                modifier = Modifier.height(44.dp)
                            )
                        }
                    }
                }
            }

            // Security & Permissions Info Card
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
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = ActionBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin-Only Privilege",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Regular app users can only watch the tutorial. Video uploading and replacement permissions are strictly restricted to staff and system administrators.",
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
