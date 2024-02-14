package com.evest.menu.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evest.menu.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
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

    init {
        viewModelScope.launch {
            _menuList.collectLatest {
                _state.update {
                    it.copy(
                        dataLoaded = true
                    )
                }
            }
        }
    }

    private val applicationContext = getApplication<Application>().applicationContext

    private val isLoggedOption = Preference.AccountPreference.options.first()

    private val preferences = applicationContext.getSharedPreferences(
        Preference.AccountPreference.name,
        Context.MODE_PRIVATE
    )

    private fun isLogged(): Boolean {
        return preferences.getBoolean(
            isLoggedOption.name,
            isLoggedOption.defaultValue as Boolean
        )
    }

    @SuppressLint("WearRecents")
    @OptIn(DelicateCoroutinesApi::class)
    fun onEvent(event: MenuEvent) {
        when (event) {
            is MenuEvent.FetchMenu -> {
                if (
                    (event.initialFetch && state.value.wereDataFetched) ||
                    (event.initialFetch && state.value.status == "error")
                ) {
                    return
                }
                GlobalScope.launch {
                    _state.update {
                        it.copy(
                            isFetchingData = true,
                            status = "loading"
                        )
                    }
                    try {
                        fetchMenu(dao, applicationContext)

                        if (isLogged()) {
                            fetchLoggedMenu(dao, applicationContext)
                        }

                        if (event.initialFetch) {
                            _state.update {
                                it.copy(wereDataFetched = true)
                            }
                        }

                        _state.update {
                            it.copy(status = "loaded")
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        startConfirmation(
                            applicationContext, applicationContext.resources.getString(
                                R.string.error_fetching
                            ), "error"
                        )

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
                    try {
                        dao.clearMenu()
                        dao.clearItem()
                        dao.clearMeal()
                        dao.clearAllergen()
                        dao.clearMealAllergenCrossRef()
                        dao.clearLoggedItem()

                        startConfirmation(
                            applicationContext, applicationContext.resources.getString(
                                R.string.database_cleared
                            )
                        )
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        startConfirmation(
                            applicationContext, applicationContext.resources.getString(
                                R.string.database_clear_error
                            ), "error"
                        )
                    }
                }
            }

            is MenuEvent.OpenAlert -> {
                val intent = Intent(applicationContext, AlertActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("ACTION_NAME", event.actionName)
                intent.putExtra("TITLE_TEXT", event.titleText)
                intent.putExtra("MESSAGE", event.message)
                applicationContext.startActivity(intent)
            }

            is MenuEvent.GetMenu -> {
                viewModelScope.launch {
                    val itemAndMealList = dao.getItemAndMealAndLoggedItemList(event.menuId)
                    val mealWithAllergensList =
                        dao.getMealWithAllergens(itemAndMealList.map { it.meal.mealId })
                    _state.update {
                        it.copy(
                            itemAndMealAndLoggedItemList = itemAndMealList,
                            mealWithAllergensList = mealWithAllergensList
                        )
                    }
                }
            }

            is MenuEvent.UpdateLoggedItems -> {
                viewModelScope.launch {
                    val itemAndMealAndLoggedItemList =
                        dao.getItemAndMealAndLoggedItemList(event.menuId)
                    _state.update {
                        it.copy(
                            itemAndMealAndLoggedItemList = itemAndMealAndLoggedItemList,
                        )
                    }
                }
            }

            is MenuEvent.GetItem -> {
                viewModelScope.launch {
                    val itemAndRelations = dao.getSingleItemAndRelations(event.itemId)
                    val mealWithAllergens =
                        dao.getMealWithAllergensById(itemAndRelations.meal.mealId)
                    _state.update {
                        it.copy(
                            itemAndRelations = itemAndRelations,
                            mealWithAllergens = mealWithAllergens
                        )
                    }
                }
            }
        }
    }
}
