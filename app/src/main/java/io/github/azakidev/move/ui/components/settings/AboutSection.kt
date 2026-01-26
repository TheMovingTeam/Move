package io.github.azakidev.move.ui.components.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.RowButton
import io.github.azakidev.move.ui.listShape

enum class AboutElement(
    val icon: ImageVector,
    @param:StringRes val description: Int,
    val link: String
) {
    Bug(
        icon = Icons.Rounded.BugReport,
        description = R.string.bugReport,
        link = "mailto:support@movetransit.app"
    ),
    Follow(
        icon = Icons.Rounded.AlternateEmail,
        description = R.string.socialMedia,
        link = "https://twitter.com/movetransit"
    ),
    Patreon(
        icon = Icons.Rounded.Favorite,
        description = R.string.support,
        link = "https://patreon.com/movetransit"
    ),
    Privacy(
        icon = Icons.Rounded.Info,
        description = R.string.privacyPolicy,
        link = "https://movetransit.app/privacy/"
    )
}

@Composable
fun AboutSection(
    modifier: Modifier = Modifier,
    onChangeLogShow: () -> Unit,
    isOnboardingComplete: Boolean,
) {
    val uriHandler = LocalUriHandler.current
    val totalButtons = if (isOnboardingComplete) AboutElement.entries.count() + 1 else AboutElement.entries.count()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = PADDING.dp, vertical = PADDING.div(2).dp),
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.titleMedium
        )
        AboutElement.entries.forEach {
            RowButton(
                shape = listShape(AboutElement.entries.indexOf(it), totalButtons),
                icon = it.icon,
                description = stringResource(it.description),
                onClick = {
                    uriHandler.openUri(it.link)
                }
            )
        }
        if (isOnboardingComplete) {
            RowButton(
                shape = listShape(AboutElement.entries.count(), totalButtons),
                icon = Icons.Rounded.NewReleases,
                description = stringResource(R.string.showChangeLog),
                onClick = onChangeLogShow
            )
        }
    }
}