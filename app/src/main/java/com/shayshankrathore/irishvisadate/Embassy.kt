package com.shayshankrathore.irishvisadate

import java.time.LocalDate

data class VacOption(
    val label: String,
    val cities: String,
    val transitDays: Int,
)

data class EmbassyContact(
    val phone: String,
    val email: String,
    val address: String,
)

data class Embassy(
    val id: String,
    val label: String,
    val flag: String,
    val vacOptions: List<VacOption>,
    val holidays: Set<LocalDate>,
    val decisionsUrl: String,
    val courierNote: String,
    val courierDays: Int,
    val contact: EmbassyContact,
    val vacBookingUrl: String = "",  // VFS Global / BLS appointment booking page
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
    courierDays = 3,
    contact = EmbassyContact(
        phone   = "+91 11 4940 3200",
        email   = "visainfo.newdelhi@dfa.ie",
        address = "230 Jor Bagh, New Delhi 110003",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/ind/en/irl",
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
    courierDays = 5,
    contact = EmbassyContact(
        phone   = "+7 495 937 5911",
        email   = "moscow@dfa.ie",
        address = "Grokholsky Per. 5, 129010 Moscow",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/rus/en/irl",
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
    courierDays = 3,
    contact = EmbassyContact(
        phone   = "+44 20 7235 2171",
        email   = "londonvisa@dfa.ie",
        address = "17 Grosvenor Place, London SW1X 7HR",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/gbr/en/irl",
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
    courierDays = 5,
    contact = EmbassyContact(
        phone   = "+86 10 6532 2691",
        email   = "beijing@dfa.ie",
        address = "3 Ritan Dong Lu, Chaoyang District, Beijing 100600",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/chn/en/irl",
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
    courierDays = 3,
    contact = EmbassyContact(
        phone   = "+90 312 446 6172",
        email   = "ankara@dfa.ie",
        address = "Uğur Mumcu Cad. 88, Gaziosmanpaşa, Ankara 06700",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/tur/en/irl",
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
    courierDays = 3,
    contact = EmbassyContact(
        phone   = "+971 2 495 8200",
        email   = "abudhabi@dfa.ie",
        address = "Al Bateen Area, Khalifa bin Zayed St, Abu Dhabi",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/are/en/irl",
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
    courierDays = 5,
    contact = EmbassyContact(
        phone   = "+92 51 831 6950",
        email   = "islamabad@dfa.ie",
        address = "Plot 1, Diplomatic Enclave, Islamabad 44000",
    ),
    vacBookingUrl = "https://www.blsinternational.com/ireland/",
)

val EMBASSY_GERMANY = Embassy(
    id = "germany",
    label = "Germany (Berlin)",
    flag = "🇩🇪",
    vacOptions = listOf(
        VacOption("Berlin", "Berlin", 1),
        VacOption("Other Cities", "Frankfurt / Munich / Hamburg / Düsseldorf", 2),
    ),
    holidays = GERMANY_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/germany/berlin/services/visas/",
    courierNote = "Plus ~2–3 working days for return courier from Irish Embassy Berlin",
    courierDays = 3,
    contact = EmbassyContact(
        phone   = "+49 30 220 720",
        email   = "berlin@dfa.ie",
        address = "Jägerstraße 51, 10117 Berlin",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/deu/en/irl",
)

val EMBASSY_FRANCE = Embassy(
    id = "france",
    label = "France (Paris)",
    flag = "🇫🇷",
    vacOptions = listOf(
        VacOption("Paris", "Paris", 1),
        VacOption("Other Cities", "Lyon / Marseille / Bordeaux / Toulouse", 2),
    ),
    holidays = FRANCE_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/france/paris/services/visas/",
    courierNote = "Plus ~2–3 working days for return courier from Irish Embassy Paris",
    courierDays = 3,
    contact = EmbassyContact(
        phone   = "+33 1 44 17 67 00",
        email   = "paris@dfa.ie",
        address = "4 Rue Rude, 75116 Paris",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/fra/en/irl",
)

val EMBASSY_NIGERIA = Embassy(
    id = "nigeria",
    label = "Nigeria (Abuja)",
    flag = "🇳🇬",
    vacOptions = listOf(
        VacOption("Lagos", "Lagos", 1),
        VacOption("Abuja", "Abuja", 2),
    ),
    holidays = NIGERIA_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/nigeria/abuja/services/visas/",
    courierNote = "Plus ~3–5 working days for return courier from Irish Embassy Abuja",
    courierDays = 5,
    contact = EmbassyContact(
        phone   = "+234 9 461 9600",
        email   = "abuja@dfa.ie",
        address = "7 Summerton Road, Maitama, Abuja",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/nga/en/irl",
)

val EMBASSY_SOUTH_AFRICA = Embassy(
    id = "southafrica",
    label = "South Africa (Pretoria)",
    flag = "🇿🇦",
    vacOptions = listOf(
        VacOption("Pretoria", "Pretoria", 1),
        VacOption("Cape Town", "Cape Town", 2),
        VacOption("Johannesburg", "Johannesburg", 2),
    ),
    holidays = SOUTH_AFRICA_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/southafrica/pretoria/services/visas/",
    courierNote = "Plus ~3–5 working days for return courier from Irish Embassy Pretoria",
    courierDays = 5,
    contact = EmbassyContact(
        phone   = "+27 12 342 5062",
        email   = "pretoria@dfa.ie",
        address = "Libertas House, 97 Gleneagles Road, Greenside, Johannesburg",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/zaf/en/irl",
)

val EMBASSY_PHILIPPINES = Embassy(
    id = "philippines",
    label = "Philippines (Manila)",
    flag = "🇵🇭",
    vacOptions = listOf(
        VacOption("Manila", "Manila", 1),
        VacOption("Cebu", "Cebu", 2),
    ),
    holidays = PHILIPPINES_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/philippines/manila/services/visas/",
    courierNote = "Plus ~3–5 working days for return courier from Irish Embassy Manila",
    courierDays = 5,
    contact = EmbassyContact(
        phone   = "+63 2 8859 2000",
        email   = "manila@dfa.ie",
        address = "70F Tower One & Exchange Plaza, Ayala Triangle, Makati City",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/phl/en/irl",
)

val EMBASSY_BANGLADESH = Embassy(
    id = "bangladesh",
    label = "Bangladesh (Dhaka)",
    flag = "🇧🇩",
    vacOptions = listOf(
        VacOption("Dhaka", "Dhaka", 1),
        VacOption("Chittagong", "Chittagong", 2),
    ),
    holidays = BANGLADESH_HOLIDAYS,
    decisionsUrl = "https://www.ireland.ie/en/bangladesh/dhaka/services/visas/",
    courierNote = "Plus ~3–5 working days for return courier from Irish Embassy Dhaka",
    courierDays = 5,
    contact = EmbassyContact(
        phone   = "+880 2 9882305",
        email   = "dhaka@dfa.ie",
        address = "House 20, Road 73, Gulshan-2, Dhaka 1212",
    ),
    vacBookingUrl = "https://visa.vfsglobal.com/bgd/en/irl",
)

val ALL_EMBASSIES: List<Embassy> = listOf(
    EMBASSY_INDIA,
    EMBASSY_MOSCOW,
    EMBASSY_LONDON,
    EMBASSY_BEIJING,
    EMBASSY_ANKARA,
    EMBASSY_ABU_DHABI,
    EMBASSY_ISLAMABAD,
    EMBASSY_GERMANY,
    EMBASSY_FRANCE,
    EMBASSY_NIGERIA,
    EMBASSY_SOUTH_AFRICA,
    EMBASSY_PHILIPPINES,
    EMBASSY_BANGLADESH,
)
