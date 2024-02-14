package com.evest.menu.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import kotlinx.coroutines.launch

@Composable
fun SettingsItem(
    preference: Preference,
    content: @Composable (SharedPreferences, Preference) -> Unit
) {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences(preference.name, Context.MODE_PRIVATE)
    }
    Column {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(stringResource(preference.label), fontSize = 20.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            content(preferences, preference)
        }
    }
}

@OptIn(ExperimentalWearFoundationApi::class)
@SuppressLint("CommitPrefEdits")
@Composable
fun SettingsScreen() {
    val listState = rememberScalingLazyListState(0)

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val focusRequester = rememberActiveFocusRequester()
            val coroutineScope = rememberCoroutineScope()

            ScalingLazyColumn(
                Modifier
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
                    Spacer(Modifier.height(20.dp))
                }
                item {
                    SettingsItem(Preference.MenuPreference) { preferences, preference ->
                        Column {
                            preference.options.forEachIndexed { index, option ->
                                if (index != 0) {
                                    Spacer(Modifier.height(5.dp))
                                }

                                var isChecked by remember {
                                    mutableStateOf(preferences.getBoolean(option.name, true))
                                }

                                ToggleChip(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(40.dp),
                                    checked = isChecked,
                                    onCheckedChange = { isNewChecked ->
                                        preferences.edit().apply {
                                            putBoolean(option.name, isNewChecked)
                                        }.apply()
                                        isChecked = isNewChecked
                                    },
                                    label = {
                                        Text(stringResource(option.label))
                                    },
                                    toggleControl = {
                                        Checkbox(isChecked)
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}