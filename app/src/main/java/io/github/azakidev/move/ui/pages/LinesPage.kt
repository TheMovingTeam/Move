package io.github.azakidev.move.ui.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.ui.components.LineRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinesPage(
    model: MoveModel = MoveModel(),
    sheetModel: SheetStopViewModel = SheetStopViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = null,
                title = {
                    Text(
                        text = stringResource(
                            R.string.lines
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(model.lines.count()) { i ->
                    val item = model.lines[i]
                    val shape = when (i) {
                        0 -> {
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 4.dp,
                            )
                        }

                        model.lines.count() - 1 -> {
                            RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 4.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        }

                        else -> {
                            MaterialTheme.shapes.extraSmall
                        }
                    }
                    LineRow(
                        model = model,
                        sheetModel = sheetModel,
                        lineItem = item,
                        shape = shape,
                    )
                }
            }
        }
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
@Preview
fun LinesPagePreview() {
    val model = MoveModel()
    val sheetModel = SheetStopViewModel()
    LinesPage(model, sheetModel)
}