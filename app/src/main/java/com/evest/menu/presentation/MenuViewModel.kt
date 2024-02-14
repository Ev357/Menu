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
    private val _menuList = dao.getMenuWithItems()
    private val _mealList = dao.getMeals()
    val state = combine(_state, _menuList, _mealList) { state, menuList, mealList ->
        state.copy(
            menuWithItemsList = menuList,
            mealList = mealList
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
                        it.copy(isFetchingData = true)
                    }
                    try {
                        fetchMenu(dao, getApplication<Application>().applicationContext)
                        if (event.initialFetch) {
                            _state.update {
                                it.copy(wereDataFetched = true)
                            }
                        }

                        _state.update {
                            it.copy(status = "loaded")
                        }
                    } catch (_: Throwable) {
                        _state.update {
                            it.copy(status = "error")
                        }
                    }
                    _state.update {
                        it.copy(isFetchingData = false)
                    }
                }
            }

            MenuEvent.ClearDatabase -> {
                viewModelScope.launch {
                    dao.clearMenu()
                    dao.clearItem()
                    dao.clearMeal()
                    dao.clearAllergen()
                    dao.clearMealAllergenCrossRef()
                }
            }
        }
    }
}
