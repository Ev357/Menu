package com.evest.menu.presentation

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.evest.menu.R
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun MealScreen(itemId: Long) {
    val listState = rememberScalingLazyListState(0)
    val context = LocalContext.current
    val dao = MenuDatabase.getInstance(context).dao
    @Suppress("UNCHECKED_CAST") val viewModel = viewModel<MenuViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MenuViewModel(dao, context.applicationContext as Application) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()

    viewModel.onEvent(MenuEvent.GetItem(itemId))

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
        ) {
            val focusRequester = rememberActiveFocusRequester()
            val coroutineScope = rememberCoroutineScope()

            ScalingLazyColumn(
                modifier = Modifier
                    .onRotaryScrollEvent {
                        coroutineScope.launch {
                            listState.scrollBy(it.verticalScrollPixels)
                            listState.animateScrollBy(0f)
                        }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                scalingParams = ScalingLazyColumnDefaults.scalingParams(
                    edgeScale = 1f,
                    edgeAlpha = 1f
                ),
                contentPadding = PaddingValues(vertical = 30.dp)
            ) {
                state.itemAndRelations?.let { itemAndRelations ->
                    item {
                        val mealLabel = getMealTypeLabel(itemAndRelations.item.type) ?: return@item
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(mealLabel))
                            Text(
                                buildAnnotatedString {
                                    withStyle(style = ParagraphStyle(lineHeight = 14.sp)) {
                                        append(itemAndRelations.meal.name)
                                    }
                                },
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                color = Color.Gray
                            )
                        }
                    }
                    itemAndRelations.loggedItem?.let { loggedItem ->
                        item {
                            val dateTimeFormatter =
                                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

                            Column(
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Text(
                                    stringResource(R.string.order_info),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(5.dp))

                                Text(
                                    buildAnnotatedString {
                                        withStyle(style = ParagraphStyle(lineHeight = 14.sp)) {
                                            append("${context.resources.getString(R.string.dispensing_from)}: ")
                                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                                append("${loggedItem.startDispensingTime}\n")
                                            }

                                            append("${context.resources.getString(R.string.to)}: ")
                                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                                append("${loggedItem.endDispensingTime}\n")
                                            }

                                            val endOrderDateTime =
                                                loggedItem.endOrderDateTime.format(dateTimeFormatter)
                                            append("${context.resources.getString(R.string.order_by)}: ")
                                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                                append("$endOrderDateTime\n")
                                            }

                                            val endCancelDateTime =
                                                loggedItem.endCancelDateTime.format(
                                                    dateTimeFormatter
                                                )
                                            append("${context.resources.getString(R.string.cancel_by)}: ")
                                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                                append(endCancelDateTime)
                                            }

                                        }
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    state.mealWithAllergens?.let { mealWithAllergens ->
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.allergens))
                                mealWithAllergens.allergens.forEach { allergen ->
                                    Text(
                                        buildAnnotatedString {
                                            withStyle(style = ParagraphStyle(lineHeight = 14.sp)) {
                                                append("${allergen.allergenId}: ")
                                                withStyle(style = SpanStyle(color = Color.Gray)) {
                                                    append(allergen.description)
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(0.9f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}