package com.evest.menu.presentation

import android.content.Context
import entities.LoggedItem
import entities.Menu
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

suspend fun fetchLoggedMenu(dao: MenuDao, context: Context) {
    if (!isInternetAvailable(context)) {
        return
    }

    val serverPreferences =
        context.getSharedPreferences(Preference.ServerPreference.name, Context.MODE_PRIVATE)
    val urlOption = Preference.ServerPreference.options.first()

    val url =
        serverPreferences.getString(urlOption.name, urlOption.defaultValue as String) ?: return

    val (jSessionId, rememberMe) = login(context)

    val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    val menuRequest = Request.Builder()
        .url("$url/faces/secured/month.jsp?terminal=false&keyboard=false&printer=false")
        .header("cookie", "JSESSIONID=$jSessionId; remember-me=$rememberMe")
        .build()

    val response = client.newCall(menuRequest).execute()

    val html = response.body?.string() ?: return

    parseLoggedMenuHtml(html, dao)
}

suspend fun parseLoggedMenuHtml(html: String, dao: MenuDao) {
    val doc = Jsoup.parse(html)

    val dayMenuElements =
        doc.select("div.mainContext.mainContextMinWidth > table > tbody > tr > td > form") ?: return

    dayMenuElements.forEach { dayMenuElement ->
        val dateString = dayMenuElement.getElementsByClass("jidelnicekTop")
            .attr("id") ?: return

        val formatter = DateTimeFormatter.ofPattern("'day'-yyyy-MM-dd")
        val date = LocalDate.parse(dateString, formatter)

        val mealContainers = dayMenuElement.getElementsByClass("jidelnicekItemWrapper") ?: return

        val menu = dao.getMenu(date) ?: return

        mealContainers.forEach { createLoggedItem(it, dao, menu) }
    }
}

suspend fun createLoggedItem(
    menuItemElement: Element,
    dao: MenuDao,
    menu: Menu
) {
    var isTaken = false
    var state = "normal"

    val clockIcon = menuItemElement.select("i.far.fa-clock.fa-2x.inlineIcon") ?: return
    val clockITitleAttr = clockIcon.attr("title")

    val startDispensingTime = parseTimeBetweenTags(clockITitleAttr, "od:") ?: return
    val endDispensingTime = parseTimeBetweenTags(clockITitleAttr, "do:") ?: return
    val endOrderDateTime = parseDateTimeBetweenTags(clockITitleAttr, "objednat do:") ?: return
    val endCancelDateTime = parseDateTimeBetweenTags(clockITitleAttr, "zrušit do:") ?: return

    val checkIcon = menuItemElement.getElementsByClass("button-link-tick")
    if (!checkIcon.isNullOrEmpty()) {
        isTaken = true
    }

    val blockIcon = menuItemElement.select("i.fa.fa-ban.fa-2x.inlineIcon.text-danger")
    if (!blockIcon.isNullOrEmpty()) {
        val blockITitleAttr = blockIcon.attr("title")
        when {
            blockITitleAttr.contains("Nemáte povoleno") -> state = "not_allowed"
            blockITitleAttr.contains("již uzavřeny") -> state = "over"
        }
    }


    val mealTypeText =
        menuItemElement.select("span.smallBoldTitle.button-link-align").text().trim()
    val mealType = getMealType(mealTypeText) ?: return

    val item = dao.getItem(menu.menuId, mealType) ?: return

    val loggedItemId = dao.upsertLoggedItem(
        LoggedItem(
            isTaken = isTaken,
            state = state,
            startDispensingTime = startDispensingTime,
            endDispensingTime = endDispensingTime,
            endOrderDateTime = endOrderDateTime,
            endCancelDateTime = endCancelDateTime,
            itemId = item.itemId
        )
    )
    if (loggedItemId.toInt() == -1) {
        dao.updateLoggedItem(
            item.itemId,
            state = state,
            startDispensingTime = startDispensingTime,
            endDispensingTime = endDispensingTime,
            endOrderDateTime = endOrderDateTime,
            endCancelDateTime = endCancelDateTime,
            isTaken = isTaken
        )
    }
}