package com.shayshankrathore.irishvisadate

import java.time.LocalDate

data class VacOption(
    val label: String,
    val cities: String,
    val transitDays: Int,
)

data class Embassy(
    val id: String,
    val label: String,
    val flag: String,
    val vacOptions: List<VacOption>,
    val holidays: Set<LocalDate>,
    val decisionsUrl: String,
    val courierNote: String,
)

val EMBASSY_INDIA = Embassy(
    id = "india",
    label = "India (New Delhi)",
    flag = "🇮🇳",
    vacOptions = listOf(
        VacOption("Delhi", "Delhi", 1),
        VacOption("Other Cities", "Mumbai / Bengaluru / Chennai / Kolkata", 2),
    ),
    holidays = INDIA_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/india/newdelhi/services/visas/processing-times-and-decisions/",
    courierNote = "Plus ~3 working days for return courier (Irish Embassy → Delhi VAC → your address via Blue Dart)",
)

val EMBASSY_MOSCOW = Embassy(
    id = "moscow",
    label = "Russia / CIS (Moscow)",
    flag = "🇷🇺",
    vacOptions = listOf(
        VacOption("Moscow", "Moscow, Russia", 1),
        VacOption("Almaty", "Almaty, Kazakhstan", 2),
        VacOption("Other CIS", "Nur-Sultan / Tashkent / Bishkek / Dushanbe", 3),
    ),
    holidays = RUSSIA_HOLIDAYS,
    decisionsUrl = "https://www.irishimmigration.ie",
    courierNote = "Plus ~3–5 working days for return courier from Irish Embassy Moscow",
)

val EMBASSY_LONDON = Embassy(
    id = "london",
    label = "UK (London)",
    flag = "🇬🇧",
    vacOptions = listOf(
        VacOption("London", "London", 1),
        VacOption("Other UK", "Manchester / Edinburgh / Birmingham / Belfast", 2),
    ),
    holidays = UK_HOLIDAYS,
    decisionsUrl = "https://www.irishimmigration.ie",
    courierNote = "Plus ~2–3 working days for return courier from Irish Embassy London",
)

val EMBASSY_BEIJING = Embassy(
    id = "beijing",
    label = "China (Beijing)",
    flag = "🇨🇳",
    vacOptions = listOf(
        VacOption("Beijing", "Beijing", 1),
        VacOption("Shanghai", "Shanghai", 2),
        VacOption("Other Cities", "Guangzhou / Chengdu / other", 3),
    ),
    holidays = CHINA_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/china/beijing/services/visas/",
    courierNote = "Plus ~3–5 working days for return courier from Irish Embassy Beijing",
)

val EMBASSY_ANKARA = Embassy(
    id = "ankara",
    label = "Turkey (Ankara)",
    flag = "🇹🇷",
    vacOptions = listOf(
        VacOption("Ankara", "Ankara", 1),
        VacOption("Istanbul", "Istanbul", 2),
    ),
    holidays = TURKEY_HOLIDAYS,
    decisionsUrl = "https://www.irishimmigration.ie",
    courierNote = "Plus ~2–3 working days for return courier from Irish Embassy Ankara",
)

val EMBASSY_ABU_DHABI = Embassy(
    id = "abudhabi",
    label = "UAE (Abu Dhabi)",
    flag = "🇦🇪",
    vacOptions = listOf(
        VacOption("Abu Dhabi", "Abu Dhabi", 1),
        VacOption("Dubai", "Dubai", 2),
    ),
    holidays = UAE_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/uae/abudhabi/services/visas/",
    courierNote = "Plus ~2–3 working days for return courier from Irish Embassy Abu Dhabi",
)

val EMBASSY_ISLAMABAD = Embassy(
    id = "islamabad",
    label = "Pakistan (Islamabad)",
    flag = "🇵🇰",
    vacOptions = listOf(
        VacOption("Islamabad", "Islamabad", 1),
        VacOption("Karachi", "Karachi", 2),
        VacOption("Lahore", "Lahore", 2),
    ),
    holidays = PAKISTAN_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/pakistan/islamabad/services/visas/",
    courierNote = "Plus ~3–5 working days for return courier from Irish Embassy Islamabad",
)

val ALL_EMBASSIES: List<Embassy> = listOf(
    EMBASSY_INDIA,
    EMBASSY_MOSCOW,
    EMBASSY_LONDON,
    EMBASSY_BEIJING,
    EMBASSY_ANKARA,
    EMBASSY_ABU_DHABI,
    EMBASSY_ISLAMABAD,
)
