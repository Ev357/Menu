package com.evest.menu.presentation

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.RevealValue
import androidx.wear.compose.foundation.SwipeToReveal
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.foundation.rememberRevealState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.SplitToggleChip
import androidx.wear.compose.material.Text
import com.evest.menu.R
import entities.relations.ItemAndMeal
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun MenuScreen(menuId: Long) {
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

    viewModel.onEvent(MenuEvent.GetMenu(menuId))

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

            val preferences =
                context.getSharedPreferences(Preference.MenuPreference.name, Context.MODE_PRIVATE)

            val filteredItemAndMealList =
                state.itemAndMealList.filter {
                    preferences.getBoolean(
                        "display_${it.item.type}",
                        true
                    )
                }

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
                scalingParams = ScalingLazyColumnDefaults.scalingParams(
                    edgeScale = 1f,
                    edgeAlpha = 1f
                ),
                contentPadding = PaddingValues(start = 10.dp)
            ) {
                itemsIndexed(
                    filteredItemAndMealList,
                    { _, (item) -> item.itemId }
                ) { index, itemAndMeal ->
                    val revealState = rememberRevealState()
                    val scope = rememberCoroutineScope()
                    val allergens =
                        state.mealWithAllergensList.find { it.meal.mealId == itemAndMeal.meal.mealId }

                    SwipeToReveal(
                        modifier = if (index == 0) Modifier.padding(top = 20.dp) else Modifier,
                        primaryAction = {
                            Column {
                                allergens?.allergens?.forEach { allergen ->
                                    Text(
                                        allergen.allergenId.toString(),
                                        maxLines = 1,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        },
                        undoAction = {
                            Row {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            revealState.animateTo(RevealValue.Covered)
                                        }
                                    },
                                    colors = ButtonDefaults.iconButtonColors()

                                ) {
                                    Icon(Icons.Default.ChevronLeft, stringResource(R.string.cancel))
                                }
                                Column {
                                    allergens?.allergens?.forEach { allergen ->
                                        Text(
                                            "${allergen.allergenId} - ${allergen.description}",
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        },
                        state = revealState,
                        onFullSwipe = {
                            /*scope.launch {
                                revealState.animateTo(RevealValue.Covered)
                            }*/
                        }
                    ) {
                        if (itemAndMeal.item.state != "unknown") {
                            LoggedItemChip(itemAndMeal)
                        } else {
                            ItemChip(itemAndMeal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoggedItemChip(itemAndMeal: ItemAndMeal) {
    val mealLabel = stringResource(getMealTypeLabel(itemAndMeal.item.type)!!)
    var checked by remember {
        mutableStateOf(
            itemAndMeal.item.state == "taken"
        )
    }

    SplitToggleChip(
        modifier = Modifier.fillMaxWidth(0.95f),
        label = {
            Text(mealLabel)
        },
        secondaryLabel = {
            Text(itemAndMeal.meal.name)
        },
        checked = checked,
        toggleControl = {
            if (itemAndMeal.item.state != "not_allowed") {
                Checkbox(
                    checked = checked,
                    enabled = true,
                )
            } else {
                Icon(Icons.Default.Block, stringResource(R.string.not_allowed))
            }

        },
        onCheckedChange = { checked = it },
        onClick = { checked = !checked },
        enabled = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun ItemChip(itemAndMeal: ItemAndMeal) {
    val mealLabel = stringResource(getMealTypeLabel(itemAndMeal.item.type)!!)

    Chip(
        modifier = Modifier.fillMaxWidth(0.95f),
        label = {
            Text(mealLabel)
        },
        secondaryLabel = {
            Text(itemAndMeal.meal.name)
        },
        shape = MaterialTheme.shapes.large,
        colors = ChipDefaults.secondaryChipColors(),
        onClick = {}
    )
}