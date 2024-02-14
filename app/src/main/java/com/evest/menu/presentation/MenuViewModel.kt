package com.evest.menu.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MenuViewModel(
    private val dao: MenuDao, application: Application
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(MenuState())
    private val _menuList = dao.getMenuAndMeal()
    val state = combine(_state, _menuList) { state, menuList ->
        state.copy(
            menuList = menuList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuState())

    @OptIn(DelicateCoroutinesApi::class)
    fun onEvent(event: MenuEvent) {
        when (event) {
            is MenuEvent.FetchMenu -> {
                if (event.initialFetch && _state.value.wereDataFetched) {
                    return
                }
                GlobalScope.launch {
                    _state.update {
                        it.copy(
                            isFetchingData = true
                        )
                    }
                    fetchMenu(event.url, dao, getApplication<Application>().applicationContext)
                    _state.update {
                        if (event.initialFetch) {
                            it.copy(
                                isFetchingData = false,
                                wereDataFetched = true
                            )
                        } else {
                            it.copy(
                                isFetchingData = false,
                            )
                        }

                    }
                }
            }

            MenuEvent.ClearDatabase -> {
                viewModelScope.launch {
                    dao.clearMenu()
                    dao.clearMeal()
                }
            }
        }
    }
}
