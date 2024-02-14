import com.evest.menu.presentation.Allergen
import com.evest.menu.presentation.Menu
import com.evest.menu.presentation.MenuItem
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun getMenuList(url: String): List<Menu> {
    val doc = Jsoup.connect(url).get()

    val menu = doc.getElementsByClass("jidelnicekDen")

    return menu.mapNotNull { element ->
        run {
            val divTextDate = element.getElementsByClass("jidelnicekTop").text()

            val pattern = """(\d{2}\.\d{2}\.\d{4})""".toRegex()
            val dateString = pattern.find(divTextDate)?.value

            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val date = LocalDate.parse(dateString, formatter)

            val menuItems = element.getElementsByClass("container")

            val breakfast = getMenuItem(menuItems, "Snídaně")
            val soup = getMenuItem(menuItems, "Polévka")
            val lunch1 = getMenuItem(menuItems, "Oběd1")
            val lunch2 = getMenuItem(menuItems, "Oběd2")
            val dinner = getMenuItem(menuItems, "Večeře")

            Menu(date, breakfast, soup, lunch1, lunch2, dinner)
        }
    }
}

fun getMenuItem(elements: Elements, mealType: String): MenuItem? {
    val menuItemElement = elements.find { element ->
        element.select(".shrinkedColumn.smallBoldTitle.jidelnicekItem > span")
            .text() == mealType
    }?.select(".column.jidelnicekItem")

    if (menuItemElement.isNullOrEmpty()) {
        return null
    }

    val name = formatText(menuItemElement.textNodes().first().toString())

    val allergens = menuItemElement.select("span.textGrey > span").mapNotNull { allergenElement ->
        Allergen(allergenElement.text().trim().toInt(), allergenElement.attr("title"))
    }

    return MenuItem(name, allergens)
}

private fun formatText(text: String): String {
    return text.trim().replace(",(?!\\s)".toRegex(), ", ").replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.ROOT
        ) else it.toString()
    }.run { if (endsWith('.')) this else "$this." }
}