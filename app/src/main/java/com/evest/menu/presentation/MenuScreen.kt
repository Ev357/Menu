package com.evest.menu.presentation

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
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
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.evest.menu.R
import entities.relations.MenuAndMeal
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MenuListItem(menu: MenuAndMeal) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val weekDay =
        DayOfWeek.from(LocalDate.parse(menu.menu.dateString, dateFormatter))
            .getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }

    TitleCard(
        onClick = {},
        title = { Text(weekDay) },
    ) {
        val preferences =
            context.getSharedPreferences(Preference.MenuPreference.name, Context.MODE_PRIVATE)
        val menuOptions = Preference.MenuPreference.options
        val meals = listOf(menu.breakfast, menu.soup, menu.lunch1, menu.lunch2, menu.dinner)

        menuOptions.forEachIndexed { index, option ->
            if (preferences.getBoolean(option.name, true)) {
                meals[index]?.let { meal ->
                    Text("${stringResource(option.label)} - ${meal.name}")
                }
            }
        }
    }
}

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun MenuScreen(navController: NavHostController) {
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

    viewModel.onEvent(
        MenuEvent.FetchMenu(
            "https://jidelnicek.roznovskastredni.cz",
            true
        )
    ) // TODO add to settings

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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Button(onClick = {
                            navController.navigate(Screen.SettingsScreen.route)
                        }) {
                            Spacer(Modifier.height(10.dp))
                            Icon(Icons.Default.Settings, stringResource(R.string.settings))
                        }

                        Button(
                            onClick = {
                                viewModel.onEvent(MenuEvent.FetchMenu("https://jidelnicek.roznovskastredni.cz"))
                            },
                            enabled = hasInternet
                        ) {
                            Spacer(Modifier.height(10.dp))
                            Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
                        }
                    }
                }
                items(state.menuList, { it.menu.dateString }) {
                    MenuListItem(it)
                }
                if (state.menuList.isEmpty()) {
                    item {
                        if (!hasInternet) {
                            Text("No Internet Connection")
                        }
                    }
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

