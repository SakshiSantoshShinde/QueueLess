package com.example.queueless_smartqueue.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.R
import com.example.queueless_smartqueue.ui.components.PrimaryButton
import com.example.queueless_smartqueue.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.queueless_logo),
                contentDescription = "QueueLess Logo",
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(26.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "QueueLess",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    color = CardWhite,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Skip the wait. Know your turn.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = CardWhite.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            PrimaryButton(
                text = "Get Started",
                onClick = onSplashFinished,
                modifier = Modifier.width(220.dp),
                backgroundColor = ActionBlue
            )
        }
    }
}
