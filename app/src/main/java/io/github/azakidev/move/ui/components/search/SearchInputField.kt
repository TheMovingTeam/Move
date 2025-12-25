package io.github.azakidev.move.ui.components.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.trailingButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchInputField(
    searchBarState: SearchBarState, textFieldState: TextFieldState
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val trailingIcon = trailingButton(
        textState = textFieldState.text.toString(),
        icon = Icons.AutoMirrored.Rounded.Backspace,
        onClick = {
            textFieldState.clearText()
        }
    )

    val isCollapsed =
        (searchBarState.currentValue == SearchBarValue.Collapsed) && !searchBarState.isAnimating

    SearchBarDefaults.InputField(
        searchBarState = searchBarState,
        textFieldState = textFieldState,
        onSearch = { focusManager.clearFocus() },
        placeholder = {
            Text(stringResource(R.string.searchPlaceholder))
        },
        leadingIcon = {
            AnimatedContent(
                targetState = isCollapsed
            ) { isCollapsed ->
                when (isCollapsed) {
                    true -> {
                        Icon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(PADDING.div(2).dp),
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search)
                        )
                    }

                    false -> {
                        IconButton(
                            onClick = {
                                scope.launch { searchBarState.animateToCollapsed() }
                            }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        },
        trailingIcon = trailingIcon
    )
}