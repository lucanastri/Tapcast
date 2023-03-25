package com.kizune.tapcast.data

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kizune.tapcast.BuildConfig
import com.kizune.tapcast.R
import com.kizune.tapcast.model.SettingItem
import com.kizune.tapcast.model.SettingCategory
import com.kizune.tapcast.model.Setting
import com.kizune.tapcast.model.SettingID

/**
 * Singleton that collects all the settings available...
 * ...for the app
 */
object SettingDataSource {
    fun getData(context: Context): List<Setting> {
        val accountCategory = SettingCategory(R.string.preference_account_category)
        val accountItem1 = SettingItem(
            id = SettingID.VIEW_PROFILE,
            title = Firebase.auth.currentUser?.displayName ?: "Account",
            summary = context.getString(R.string.preference_view_summary),
            showChevron = true
        )
        val accountItem2 = SettingItem(
            id = SettingID.LOGOUT,
            title = context.getString(R.string.account_logout_title),
            summary = context.getString(R.string.account_logout_summary)
        )
        val accountItem3 = SettingItem(
            id = SettingID.DELETE_PROFILE,
            title = context.getString(R.string.delete_profile_title),
            summary = context.getString(R.string.delete_profile_summary)
        )

        val streamingCategory = SettingCategory(R.string.preference_streaming_category)
        val streamingItem1 = SettingItem(
            id = SettingID.AUTOPLAY,
            title = context.getString(R.string.preference_autoplay_title),
            summary = "Off"
        )
        val streamingItem2 = SettingItem(
            id = SettingID.EXPLICIT,
            title = context.getString(R.string.preference_explicit_title),
            summary = "Off"
        )
        val streamingItem3 = SettingItem(
            id = SettingID.INTERRUPTION,
            title = context.getString(R.string.preference_interruption_title),
            summary = "On"
        )
        val infoCategory = SettingCategory(R.string.preference_info_category)
        val infoItem1 = SettingItem(
            id = SettingID.VERSION,
            title = context.getString(R.string.preference_version_title),
            summary = BuildConfig.VERSION_NAME
        )
        val infoItem2 = SettingItem(
            id = SettingID.TERMS,
            title = context.getString(R.string.preference_terms_title),
            summary = context.getString(R.string.preference_terms_summary)
        )
        val infoItem3 = SettingItem(
            id = SettingID.PRIVACY,
            title = context.getString(R.string.preference_privacy_title),
            summary = context.getString(R.string.preference_privacy_summary)
        )

        return listOf(
            accountCategory,
            accountItem1,
            accountItem2,
            accountItem3,
            streamingCategory,
            streamingItem1,
            streamingItem2,
            streamingItem3,
            infoCategory,
            infoItem1,
            infoItem2,
            infoItem3
        )
    }
}