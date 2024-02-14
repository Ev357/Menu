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
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.SplitToggleChip
import androidx.wear.compose.material.Text
import com.evest.menu.R
import entities.relations.ItemAndMealAndLoggedItem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
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

    val isLoggedOption = Preference.AccountPreference.options.first()

    val accountPreferences = remember {
        context.getSharedPreferences(Preference.AccountPreference.name, Context.MODE_PRIVATE)
    }

    val isLogged by mutableStateOf(
        accountPreferences.getBoolean(
            isLoggedOption.name,
            isLoggedOption.defaultValue as Boolean
        )
    )

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

            val menuPreferences =
                context.getSharedPreferences(Preference.MenuPreference.name, Context.MODE_PRIVATE)

            val filteredItemAndMealAndLoggedItemList =
                state.itemAndMealAndLoggedItemList.filter {
                    menuPreferences.getBoolean(
                        "display_${it.item.type}",
                        true
                    )
                }

            val hasInternet = isInternetAvailable(context)

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
                    filteredItemAndMealAndLoggedItemList,
                    { _, (item) -> item.itemId }
                ) { index, itemAndMealAndLoggedItem ->
                    val revealState = rememberRevealState()
                    val scope = rememberCoroutineScope()
                    val allergens =
                        state.mealWithAllergensList.find { it.meal.mealId == itemAndMealAndLoggedItem.meal.mealId }

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
                    ) {
                        if (isLogged && itemAndMealAndLoggedItem.item.type != "soup") {
                            LoggedItemChip(
                                itemAndMealAndLoggedItem,
                                dao,
                                viewModel::onEvent,
                                hasInternet
                            )
                        } else {
                            ItemChip(itemAndMealAndLoggedItem)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun LoggedItemChip(
    itemAndMealAndLoggedItem: ItemAndMealAndLoggedItem,
    dao: MenuDao,
    onEvent: (MenuEvent) -> Unit,
    hasInternet: Boolean
) {
    val context = LocalContext.current
    val mealLabel = stringResource(getMealTypeLabel(itemAndMealAndLoggedItem.item.type)!!)
    var checked by mutableStateOf(
        itemAndMealAndLoggedItem.loggedItem?.isTaken == true
    )
    var isLoading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    SplitToggleChip(
        modifier = Modifier.fillMaxWidth(0.95f),
        label = {
            Text(mealLabel)
        },
        secondaryLabel = {
            Text(itemAndMealAndLoggedItem.meal.name)
        },
        checked = checked,
        toggleControl = {
            if (isLoading) {
                CircularProgressIndicator(
                    indicatorColor = MaterialTheme.colors.secondary,
                    trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                    strokeWidth = 2.dp
                )
            } else {
                if (itemAndMealAndLoggedItem.loggedItem?.state != "not_allowed") {
                    Checkbox(
                        checked = checked,
                        enabled = itemAndMealAndLoggedItem.loggedItem?.state != "over" && hasInternet,
                        onCheckedChange = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    val job = GlobalScope.launch {
                                        postData(context, itemAndMealAndLoggedItem, dao)
                                    }
                                    job.join()
                                    onEvent(MenuEvent.UpdateLoggedItems(itemAndMealAndLoggedItem.item.menuId))
                                } catch (e: Throwable) {
                                    startConfirmation(
                                        context,
                                        context.resources.getString(R.string.error),
                                        "error"
                                    )
                                }
                                isLoading = false
                            }
                            checked = it
                        }
                    )
                } else {
                    Icon(Icons.Default.Block, stringResource(R.string.not_allowed))
                }
            }
        },
        onCheckedChange = { checked = it },
        onClick = { checked = !checked },
        enabled = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun ItemChip(itemAndMealAndLoggedItem: ItemAndMealAndLoggedItem) {
    val mealLabel = stringResource(getMealTypeLabel(itemAndMealAndLoggedItem.item.type)!!)

    Chip(
        modifier = Modifier.fillMaxWidth(0.95f),
        label = {
            Text(mealLabel)
        },
        secondaryLabel = {
            Text(itemAndMealAndLoggedItem.meal.name)
        },
        shape = MaterialTheme.shapes.large,
        colors = ChipDefaults.secondaryChipColors(),
        onClick = {}
    )
}