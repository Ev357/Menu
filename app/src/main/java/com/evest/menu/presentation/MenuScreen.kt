package com.evest.menu.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
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
import com.evest.menu.presentation.theme.MenuTheme
import getMenuList
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MenuListItem(menu: Menu) {
    val context = LocalContext.current

    val weekDay =
        DayOfWeek.from(menu.date).getDisplayName(TextStyle.FULL, Locale.getDefault())
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
        val menuItems = listOf(menu.breakfast, menu.soup, menu.lunch1, menu.lunch2, menu.dinner)

        menuOptions.forEachIndexed { index, option ->
            if (preferences.getBoolean(option.name, true)) {
                menuItems[index]?.let { menuItem ->
                    Text("${stringResource(option.label)} - ${menuItem.name}")
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalWearFoundationApi::class, DelicateCoroutinesApi::class)
@Composable
fun MenuScreen(navController: NavHostController) {
    var menuList by remember {
        mutableStateOf<List<Menu>?>(null)
    }

    GlobalScope.launch(Dispatchers.IO) {
        menuList = async { getMenuList("https://jidelnicek.roznovskastredni.cz") }.await()
    }

    MenuTheme {
        val listState = rememberScalingLazyListState(0)

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
                    menuList?.let { safeMenuList ->
                        item {
                            Button(onClick = {
                                navController.navigate(Screen.SettingsScreen.route)
                            }) {
                                Spacer(Modifier.height(10.dp))
                                Icon(Icons.Default.Settings, stringResource(R.string.settings))
                            }
                        }
                        items(safeMenuList, { it.date }) { menu ->
                            MenuListItem(menu)
                        }
                        item {
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
                if (menuList == null) {
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
}
