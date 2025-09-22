package io.github.azakidev.move.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable @Preview
fun LogoHero(
    shapeAngle:Int = 0
) {
    Box(
        modifier = Modifier
            .size(208.dp)
            .clip(MaterialShapes.Cookie12Sided.toShape(shapeAngle))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorResource(R.color.purple_brand),
                        colorResource(R.color.purple_shadow)
                    )
                )
            ), contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(128.dp),
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = Color.White
        )
    }
}
