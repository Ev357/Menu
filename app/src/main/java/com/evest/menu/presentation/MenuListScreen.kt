package com.evest.menu.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.evest.menu.R
import entities.relations.MenuWithItems
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MenuListItem(menuWithItems: MenuWithItems, state: MenuState, navController: NavHostController) {
    val context = LocalContext.current

    val weekDay =
        DayOfWeek.from(menuWithItems.menu.date)
            .getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }

    val titleCardDate = getDayMonth(menuWithItems.menu.date)

    val preferences =
        context.getSharedPreferences(Preference.MenuPreference.name, Context.MODE_PRIVATE)

    val filteredMenuWithItems =
        menuWithItems.items.filter { preferences.getBoolean("display_${it.type}", true) }

    if (filteredMenuWithItems.isEmpty()) {
        return
    }

    TitleCard(
        onClick = {
            navController.navigate(Screen.MenuScreen.withArgs(menuWithItems.menu.menuId))
        },
        title = { Text(weekDay, fontSize = 14.sp) },
        time = {
            Text(titleCardDate, fontSize = 14.sp)
        },
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            MaterialTheme.colors.surface,
            MaterialTheme.colors.surface
        ),
    ) {
        filteredMenuWithItems.forEachIndexed { index, item ->
            val meal = state.mealList.find { it.mealId == item.mealId }
            val mealLabel = stringResource(getMealTypeLabel(item.type)!!)
            meal?.let {
                if (index != 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Column {
                    Text(
                        mealLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(meal.name)
                }
            }
        }
    }
}

@OptIn(ExperimentalWearFoundationApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun MenuListScreen(navController: NavHostController) {
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

    viewModel.onEvent(MenuEvent.FetchMenu(true))

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            val focusRequester = rememberActiveFocusRequester()
            val coroutineScope = rememberCoroutineScope()
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
                state = listState
            ) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            Modifier.fillMaxWidth(0.9f),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Button(
                                    onClick = {
                                        navController.navigate(Screen.SettingsScreen.route)
                                    },
                                    colors = ButtonDefaults.secondaryButtonColors()
                                ) {
                                    Spacer(Modifier.height(10.dp))
                                    Icon(Icons.Default.Settings, stringResource(R.string.settings))
                                }

                                Button(
                                    onClick = {
                                        viewModel.onEvent(MenuEvent.FetchMenu())
                                    },
                                    colors = ButtonDefaults.secondaryButtonColors(),
                                    enabled = hasInternet
                                ) {
                                    Spacer(Modifier.height(10.dp))
                                    Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
                                }
                            }
                        }

                        if (state.menuWithItemsList.isEmpty() && !hasInternet) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Icon(
                                    Icons.Default.CloudOff,
                                    stringResource(R.string.no_internet)
                                )
                                Text(stringResource(R.string.no_internet))
                            }
                        }
                        if (state.status == "error") {
                            Row(
                                Modifier.fillMaxWidth(0.9f),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    stringResource(R.string.error),
                                    tint = MaterialTheme.colors.error
                                )
                                Text(
                                    stringResource(R.string.error_fetching),
                                    color = MaterialTheme.colors.error
                                )
                            }
                        }
                    }
                }
                items(state.menuWithItemsList, { it.menu.menuId }) {
                    MenuListItem(it, state, navController)
                }
                item {
                    Spacer(Modifier.height(20.dp))
                }
            }
            if (state.isFetchingData) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    indicatorColor = MaterialTheme.colors.secondary,
                    trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

