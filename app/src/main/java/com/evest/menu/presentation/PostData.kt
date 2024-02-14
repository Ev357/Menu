package com.evest.menu.presentation

import android.content.Context
import entities.relations.ItemAndMealAndLoggedItem
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

suspend fun postData(
    context: Context,
    itemAndRelations: ItemAndMealAndLoggedItem,
    dao: MenuDao
) {
    val serverPreferences =
        context.getSharedPreferences(Preference.ServerPreference.name, Context.MODE_PRIVATE)
    val urlOption = Preference.ServerPreference.options.first()

    val url =
        serverPreferences.getString(urlOption.name, urlOption.defaultValue as String) ?: return

    val accountPreferences =
        context.getSharedPreferences(Preference.AccountPreference.name, Context.MODE_PRIVATE)

    val usernameOption = Preference.AccountPreference.options[1]
    val passwordOption = Preference.AccountPreference.options[2]

    val encryptedUsername =
        accountPreferences.getString(usernameOption.name, usernameOption.defaultValue as String)
            ?: return

    val encryptedPassword =
        accountPreferences.getString(passwordOption.name, passwordOption.defaultValue as String)
            ?: return

    val cryptoManager = CryptoManager()

    val username = cryptoManager.decrypt(encryptedUsername)
    val password = cryptoManager.decrypt(encryptedPassword)

    val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    val getRequest = Request.Builder()
        .url(url)
        .build()

    val getResponse = client.newCall(getRequest).execute()

    val initialJSessionId = getCookieValue(getResponse, "JSESSIONID")
    val initialXsrfToken = getCookieValue(getResponse, "XSRF-TOKEN") ?: return

    val formBody = FormBody.Builder()
        .add("j_username", username)
        .add("j_password", password)
        .add("_spring_security_remember_me", "on")
        .add("terminal", "false")
        .add("_csrf", initialXsrfToken)
        .add("targetUrl", "/faces/secured/main.jsp?terminal=false&status=true&printer=&keyboard=")
        .build()

    val postRequest = Request.Builder()
        .url("$url/j_spring_security_check")
        .post(formBody)
        .header("cookie", "XSRF-TOKEN=$initialXsrfToken; JSESSIONID=$initialJSessionId")
        .build()

    val postResponse = client.newCall(postRequest).execute()

    val jSessionId = getCookieValue(postResponse, "JSESSIONID")
    val rememberMe = getCookieValue(postResponse, "remember-me")

    if (jSessionId.isNullOrEmpty() || rememberMe.isNullOrEmpty()) {
        throw Exception("Login Failed")
    }

    val menuRequest = Request.Builder()
        .url("$url/faces/secured/month.jsp?terminal=false&keyboard=false&printer=false")
        .header("cookie", "JSESSIONID=$jSessionId; remember-me=$rememberMe")
        .build()

    val response = client.newCall(menuRequest).execute()

    val html = response.body?.string() ?: return

    parseMenuListHtml(html, itemAndRelations, dao, url, jSessionId, context)
}

suspend fun parseMenuListHtml(
    html: String,
    itemAndRelations: ItemAndMealAndLoggedItem,
    dao: MenuDao,
    url: String,
    jSessionId: String,
    context: Context
) {
    val doc = Jsoup.parse(html)

    val dayMenuElements =
        doc.select("div.mainContext.mainContextMinWidth > table > tbody > tr > td > form") ?: return

    val menu = dao.getMenuById(itemAndRelations.item.menuId) ?: return

    val dayMenuElement = dayMenuElements.find { dayMenuElement ->
        val dateString = dayMenuElement.getElementsByClass("jidelnicekTop")
            .attr("id") ?: return

        val formatter = DateTimeFormatter.ofPattern("'day'-yyyy-MM-dd")
        val date = LocalDate.parse(dateString, formatter)

        date == menu.date
    } ?: return

    parseMenuHtml(dayMenuElement, itemAndRelations, url, jSessionId, dao, context)
}

suspend fun parseMenuHtml(
    element: Element,
    itemAndRelations: ItemAndMealAndLoggedItem,
    url: String,
    jSessionId: String,
    dao: MenuDao,
    context: Context
) {
    val mealContainers = element.getElementsByClass("jidelnicekItemWrapper") ?: return

    val mealContainer = findMealElement(mealContainers, itemAndRelations.item.type) ?: return

    val aOnClickAttr =
        mealContainer.select("div.jidWrapLeft > a.btn.button-link.button-link-main.maxbutton")
            .attr("onclick").trim()

    val payload = aOnClickAttr.split("'")[1]

    val unixTime = System.currentTimeMillis()

    val orderType = getOrderType(itemAndRelations, mealContainers)

    val postRequest = Request.Builder()
        .url("$url/faces/secured/$payload&_=$unixTime".replace("&type=none", "&type=$orderType"))
        .header("cookie", "JSESSIONID=$jSessionId")
        .build()

    val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    client.newCall(postRequest).execute()

    fetchLoggedMenu(dao, context)
}

fun findMealElement(mealContainers: Elements, mealType: String): Element? {
    return mealContainers.find { menuItemElement ->
        val mealTypeText =
            menuItemElement.select("span.smallBoldTitle.button-link-align").text().trim()
        val itemMealType = getMealType(mealTypeText) ?: return null

        itemMealType == mealType
    }
}

fun getOrderType(itemAndRelations: ItemAndMealAndLoggedItem, mealContainers: Elements): String {
    if (itemAndRelations.item.type.startsWith("lunch")) {
        if (itemAndRelations.loggedItem?.isTaken == true) {
            return "delete"
        }

        val lunchToFind = if (itemAndRelations.item.type == "lunch1") "lunch2" else "lunch1"

        val otherLunchElement = findMealElement(mealContainers, lunchToFind)

        val checkIcon = otherLunchElement?.getElementsByClass("button-link-tick")
        return if (!checkIcon.isNullOrEmpty()) {
            "reorder"
        } else {
            "make"
        }
    } else {
        return if (itemAndRelations.loggedItem?.isTaken == true) {
            "delete"
        } else {
            "reorder"
        }
    }
}