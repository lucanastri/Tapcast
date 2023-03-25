package com.kizune.tapcast.model

import androidx.annotation.StringRes

data class SettingCategory(
    @StringRes val title: Int
) : Setting
