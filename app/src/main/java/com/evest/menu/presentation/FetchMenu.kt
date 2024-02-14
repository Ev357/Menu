package com.evest.menu.presentation

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import entities.Meal
import entities.Menu
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

suspend fun fetchMenu(url: String, dao: MenuDao, context: Context) {
    if (!isInternetAvailable(context)) {
        return
    }

    val doc = Jsoup.connect(url).get()

    val menu = doc.getElementsByClass("jidelnicekDen")

    menu.forEach { element ->
        run {
            val divTextDate = element.getElementsByClass("jidelnicekTop").text()
            if (divTextDate.isNullOrEmpty()) {
                return
            }

            val pattern = """(\d{2}\.\d{2}\.\d{4})""".toRegex()
            val dateString = pattern.find(divTextDate)?.value
            if (dateString.isNullOrEmpty()) {
                return
            }

            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val databaseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDate.parse(dateString, formatter)

            val meals = element.getElementsByClass("container")
            if (meals.isNullOrEmpty()) {
                return
            }

            val breakfastId = getMeal(meals, "Snídaně", dao)
            val soupId = getMeal(meals, "Polévka", dao)
            val lunch1Id = getMeal(meals, "Oběd1", dao)
            val lunch2Id = getMeal(meals, "Oběd2", dao)
            val dinnerId = getMeal(meals, "Večeře", dao)

            dao.upsertMenu(
                Menu(
                    databaseFormatter.format(date),
                    breakfastId,
                    soupId,
                    lunch1Id,
                    lunch2Id,
                    dinnerId
                )
            )
        }
    }
}

suspend fun getMeal(elements: Elements, mealType: String, dao: MenuDao): String? {
    val mealElement = elements.find { element ->
        element.select(".shrinkedColumn.smallBoldTitle.jidelnicekItem > span")
            .text() == mealType
    }?.select(".column.jidelnicekItem")

    if (mealElement.isNullOrEmpty()) {
        return null
    }

    val mealName = formatText(mealElement.textNodes().first().toString())

    var mealId = UUID.randomUUID().toString()
    try {
        dao.insertMeal(Meal(mealName, mealId))
    } catch (_: SQLiteConstraintException) {
        val meal = dao.getMealByName(mealName)
        mealId = meal.mealId
    }


    mealElement.select("span.textGrey > span").forEach { allergenElement ->
        dao
    }
    //Allergen(allergenElement.text().trim().toInt(), allergenElement.attr("title"))
    //}

    return mealId
}