package com.kizune.tapcast.model

data class SettingItem(
    val id: SettingID,
    val title: String,
    val summary: String,
    val showChevron: Boolean = false
) : Setting

enum class SettingID {
    VIEW_PROFILE,
    LOGOUT,
    DELETE_PROFILE,
    AUTOPLAY,
    EXPLICIT,
    INTERRUPTION,
    VERSION,
    TERMS,
    PRIVACY
}
