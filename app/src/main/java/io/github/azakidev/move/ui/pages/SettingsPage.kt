package io.github.azakidev.move.ui.pages

import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.MainView
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsPage(
    model: MoveModel,
    backStack: NavBackStack
) {
    val state = rememberTextFieldState(
        initialText = model.providerRepo.value,
    )
    val context = LocalContext.current
    val invalidText = stringResource(R.string.providerInvalid)

    val enterTransition = remember {
        slideInHorizontally(
            initialOffsetX = { it/2 },
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
            targetOffsetX = { it/2 },
            animationSpec = MotionScheme.expressive().defaultSpatialSpec()
        ) + fadeOut(
            animationSpec = MotionScheme.expressive().defaultEffectsSpec()
        ) + scaleOut(
            targetScale = 0.8f,
            animationSpec = MotionScheme.expressive().defaultSpatialSpec()
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            backStack.removeLastOrNull()
                            if (URLUtil.isValidUrl(state.text.toString()) && model.tryRepo(
                                    state.text.toString()
                                )
                            ) {
                                model.providerRepo.value = state.text.toString()
                                print(model.providerRepo.value)
                            } else {
                                Toast
                                    .makeText(context, invalidText, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clip(shape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(shape = RoundedCornerShape((15 + 8).dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.providerSource),
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        shape = RoundedCornerShape((15 / 2 + 8).dp),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.providerSource)
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = state.text.toString() != model.providerRepo.value,
                                enter = enterTransition,
                                exit = exitTransition
                            ) {
                                IconButton(
                                    onClick = {
                                        if (URLUtil.isValidUrl(state.text.toString()) && model.tryRepo(
                                                state.text.toString()
                                            )
                                        ) {
                                            model.providerRepo.value = state.text.toString()
                                            print(model.providerRepo.value)
                                        } else {
                                            Toast
                                                .makeText(context, invalidText, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun SettingsPagePreview() {
    val model = viewModel<MoveModel>()
    val backStack = rememberNavBackStack(MainView)
    SettingsPage(model, backStack)
}