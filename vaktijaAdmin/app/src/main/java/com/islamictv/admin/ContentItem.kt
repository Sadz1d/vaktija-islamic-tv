package com.islamictv.admin

data class ContentItem(
    var id: String = "",
    var type: String = "", // "hadith", "verse", "announcement"
    var arabicText: String = "",
    var bosnianText: String = "",
    var reference: String = "",
    var duration: Int = 15,
    var timestamp: Long = 0,
    var active: Boolean = true,
    var imageUrl: String = ""
)