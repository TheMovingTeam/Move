package io.github.azakidev.move.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Timer
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun trailingButton(
    textState: String,
    defaultText: String = "",
    icon: ImageVector,
    onClick: () -> Unit,
): @Composable (() -> Unit)? {
    val enterTransition = remember {
        slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = MotionScheme.expressive().defaultSpatialSpec()
        ) + fadeIn(
            animationSpec = MotionScheme.expressive().defaultEffectsSpec()
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = MotionScheme.expressive().defaultSpatialSpec()
        )
    }
    val exitTransition = remember {
        slideOutHorizontally(
            targetOffsetX = { it / 2 },
            animationSpec = MotionScheme.expressive().defaultSpatialSpec()
        ) + fadeOut(
            animationSpec = MotionScheme.expressive().defaultEffectsSpec()
        ) + scaleOut(
            targetScale = 0.8f,
            animationSpec = MotionScheme.expressive().defaultSpatialSpec()
        )
    }

    val iconState = textState != defaultText

    val shouldShowIcon = remember { mutableStateOf(false) }

    when (iconState) {
        true -> shouldShowIcon.value = true
        false -> Timer().schedule(delay = 200, action = { shouldShowIcon.value = false })
    }

    return if (shouldShowIcon.value) {
        @Composable {
            val shouldBeVisible = remember { mutableStateOf(false) }
            Timer().schedule(delay = 25, action = { shouldBeVisible.value = true })

            AnimatedVisibility(
                visible = shouldBeVisible.value && iconState,
                enter = enterTransition,
                exit = exitTransition
            ) {
                IconButton(
                    onClick = {
                        onClick()
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
        }
    } else null
}