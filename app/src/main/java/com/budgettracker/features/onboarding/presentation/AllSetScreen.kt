package com.budgettracker.features.onboarding.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.budgettracker.core.utils.AnalyticsTracker

/**
 * Confetti particle data
 */
private data class ConfettiParticle(
    val id: Int,
    val startX: Dp,
    val startY: Dp,
    val color: Color,
    val size: Dp,
    val rotation: Float,
    val emoji: String
)

/**
 * Screen 3: All Set (Success)
 * Celebratory screen shown after completing onboarding with confetti animation
 */
@Composable
fun AllSetScreen(
    displayName: String,
    onGoToDashboard: () -> Unit,
    onReviewInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track screen view
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("AllSet")
    }
    
    // Animation states
    var visible by remember { mutableStateOf(false) }
    
    // Confetti particles
    val confettiParticles = remember {
        List(50) { index ->
            ConfettiParticle(
                id = index,
                startX = Random.nextInt(0, 400).dp,
                startY = (-100 - Random.nextInt(0, 200)).dp,
                color = listOf(
                    Color(0xFFFFC107), // Amber
                    Color(0xFFFF5722), // Deep Orange
                    Color(0xFF4CAF50), // Green
                    Color(0xFF2196F3), // Blue
                    Color(0xFFE91E63), // Pink
                    Color(0xFF9C27B0)  // Purple
                ).random(),
                size = Random.nextInt(6, 12).dp,
                rotation = Random.nextFloat() * 360f,
                emoji = listOf("🎉", "✨", "🎊", "⭐", "💫", "🌟").random()
            )
        }
    }
    
    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9), // Light green
                        Color(0xFFF1F8E9), // Lighter green/yellow
                        Color(0xFFFFFDE7)  // Very light yellow
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Confetti animation
        confettiParticles.forEach { particle ->
            ConfettiParticleAnimation(
                particle = particle,
                visible = visible
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success Icon with animation
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color(0xFF4CAF50), // Green
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text(
                text = "You're all set!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32), // Dark green
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Personalized welcome
            Text(
                text = "Welcome, $displayName!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF388E3C), // Medium green
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = "Your budget is ready to go. Let's start building your financial journey.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF424242), // Dark gray
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress indicator (full)
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(4.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE8F5E9),
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Primary Button - Go to Dashboard
            Button(
                onClick = onGoToDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32), // Dark green
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Go to Dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Button - Review Info
            TextButton(
                onClick = onReviewInfo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Review your info",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF388E3C), // Medium green
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Confetti particle animation composable
 */
@Composable
private fun ConfettiParticleAnimation(
    particle: ConfettiParticle,
    visible: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti_${particle.id}")
    
    // Y position animation (falling down)
    val offsetY by infiniteTransition.animateFloat(
        initialValue = particle.startY.value,
        targetValue = particle.startY.value + 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000 + (particle.id % 1000), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetY"
    )
    
    // X position animation (swaying left and right)
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f * sin(particle.rotation),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    
    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = particle.rotation,
        targetValue = particle.rotation + 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Alpha animation (fade out as it falls)
    val alpha = remember(offsetY) {
        ((1000f - (offsetY - particle.startY.value)) / 1000f).coerceIn(0f, 1f)
    }
    
    if (visible && alpha > 0f) {
        Box(
            modifier = Modifier
                .offset(x = (particle.startX.value + offsetX).dp, y = offsetY.dp)
                .size(particle.size)
                .alpha(alpha)
        ) {
            Text(
                text = particle.emoji,
                fontSize = particle.size.value.sp,
                modifier = Modifier.graphicsLayer(rotationZ = rotation)
            )
        }
    }
}

