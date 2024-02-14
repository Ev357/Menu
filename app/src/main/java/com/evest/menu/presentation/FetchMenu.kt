package com.evest.menu.presentation

import android.content.Context
import entities.Allergen
import entities.Item
import entities.Meal
import entities.Menu
import entities.relations.MealAllergenCrossRef
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

suspend fun fetchMenu(dao: MenuDao, context: Context) {
    if (!isInternetAvailable(context)) {
        return
    }

    val preferences =
        context.getSharedPreferences(Preference.ServerPreference.name, Context.MODE_PRIVATE)
    val option = Preference.ServerPreference.options.first()
    val url = preferences.getString(option.name, option.defaultValue as String)
    if (url.isNullOrEmpty()) {
        return
    }

    val doc = Jsoup.connect(url).header("ngrok-skip-browser-warning", "69420").get()

    val dayMenuElements = doc.getElementsByClass("jidelnicekDen")
    if (dayMenuElements.isNullOrEmpty()) {
        return
    }

    val menuDateStringList = dayMenuElements.mapNotNull { dayMenuElement ->
        val dateString = dayMenuElement.getElementsByClass("jidelnicekTop")
            .attr("id")
        if (dateString.isNullOrEmpty()) {
            return
        }

        val formatter = DateTimeFormatter.ofPattern("'day'-yyyy-MM-dd")
        val date = LocalDate.parse(dateString, formatter)

        val mealContainers = dayMenuElement.getElementsByClass("container")
        if (mealContainers.isNullOrEmpty()) {
            return
        }

        val mealList = mealContainers.mapNotNull { createMeal(it, dao) }

        var menuId = dao.upsertMenu(Menu(date))
        if (menuId.toInt() == -1) {
            val newMenuId = dao.getMenu(date)?.menuId
            if (newMenuId == null || newMenuId.toInt() == -1) {
                return
            }
            menuId = newMenuId
        }

        val mealTypeList = mealList.map { it.second }

        dao.deleteRemovedItems(menuId, mealTypeList)

        mealList.forEach {
            val itemId = dao.upsertItem(Item(it.second, menuId, it.first))
            if (itemId.toInt() == -1) {
                dao.updateMenuItem(menuId, it.second, it.first)
            }
        }

        DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
    }

    if (menuDateStringList.isEmpty()) {
        return
    }

    val (newestDateString, oldestDateString) = getNewestOldestDateString(menuDateStringList)

    dao.deleteRemovedMenu(newestDateString, oldestDateString, menuDateStringList)
}

suspend fun createMeal(menuItemElement: Element, dao: MenuDao): Pair<Long, String>? {
    val mealTypeText =
        menuItemElement.select(".shrinkedColumn.smallBoldTitle.jidelnicekItem > span").text().trim()
    val mealType = getMealType(mealTypeText)
    if (mealType.isNullOrEmpty()) {
        return null
    }

    val mealElement = menuItemElement.select(".column.jidelnicekItem")
    if (mealElement.isNullOrEmpty()) {
        return null
    }

    val mealName = formatText(mealElement.textNodes().first().toString())

    var mealId = dao.upsertMeal(Meal(mealName))
    if (mealId.toInt() == -1) {
        val newMealId = dao.getMeal(mealName)?.mealId
        if (newMealId == null || newMealId.toInt() == -1) {
            return null
        }
        mealId = newMealId
    }

    mealElement.select("span.textGrey > span").forEach { allergenElement ->
        val allergenId = allergenElement.text().trim().toInt()
        dao.upsertAllergen(
            Allergen(
                allergenId,
                formatText(allergenElement.attr("title"))
            )
        )
        dao.upsertMealAllergenCrossRef(MealAllergenCrossRef(mealId, allergenId))
    }

    return mealId to mealType
}
