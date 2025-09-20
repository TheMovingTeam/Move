package io.github.azakidev.move.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.AddRoad
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.MainView
import io.github.azakidev.move.Providers
import io.github.azakidev.move.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsPage(
    providerRepo: MutableState<String>,
    backStack: NavBackStack<NavKey>,
    onProviderReset: (String) -> Unit,
    onboardingIsComplete: Boolean,
    onAppReset: () -> Unit,
) {
    val state = rememberTextFieldState(
        initialText = providerRepo.value,
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        text = stringResource(R.string.settings)
                    )
                },
                navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = {
                            backStack.removeLastOrNull()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .clip(shape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            item {
                ProviderSection(
                    state,
                    providerRepo,
                    onProviderReset,
                    onBack = {
                        backStack.add(Providers)
                    },
                    onboardingIsComplete
                )
            }
            if (onboardingIsComplete) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = stringResource(R.string.reset),
                        style = MaterialTheme.typography.titleMedium
                    )
                    ResetButtonRow(
                        onClick = onAppReset
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun SettingsPagePreview() {
    val providerRepo = remember { mutableStateOf("") }
    val backStack = rememberNavBackStack(MainView)
    SettingsPage(
        providerRepo = providerRepo,
        backStack = backStack,
        onProviderReset = {},
        onboardingIsComplete = true,
        onAppReset = {}
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProviderSection(
    state: TextFieldState,
    providerRepo: MutableState<String>,
    onClick: (String) -> Unit,
    onBack: () -> Unit,
    onboardingIsComplete: Boolean
    ) {
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
    Column(
        modifier = Modifier
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
            .clip(
                shape = RoundedCornerShape(
                    topStart = (15 + 8).dp,
                    topEnd = (15 + 8).dp,
                    4.dp,
                    4.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                text = stringResource(R.string.providerSource),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                onClick = {
                    state.edit {
                        replace(0, state.text.length, "https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/main")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Restore,
                    contentDescription = null
                )
            }
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                    visible = state.text.toString() != providerRepo.value,
                    enter = enterTransition,
                    exit = exitTransition
                ) {
                    IconButton(
                        onClick = { onClick(state.text.toString()) }
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
    if (onboardingIsComplete) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(shape = RoundedCornerShape(4.dp, 4.dp, 23.dp, 23.dp))
                .clickable(
                    onClick = onBack
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryFixedDim)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddRoad,
                        contentDescription = stringResource(R.string.providerTitle),
                        tint = MaterialTheme.colorScheme.onSecondaryFixed
                    )
                }
                Text(
                    text = stringResource(R.string.savedProviders)
                )
            }
        }
    }
}

@Composable @Preview
fun ResetButtonRow(
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(shape = RoundedCornerShape(23.dp))
            .clickable(
                onClick = onClick
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.BugReport,
                    contentDescription = stringResource(R.string.resetDesc),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                text = stringResource(R.string.resetDesc)
            )
        }
    }
}