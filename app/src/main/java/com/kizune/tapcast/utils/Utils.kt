package com.kizune.tapcast.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.media3.session.MediaController
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kizune.tapcast.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Function that takes a px value and transforms it into dp value
 */
fun Int.toDp(): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_PX,
    this.toFloat(),
    Resources.getSystem().displayMetrics
).toInt()

/**
 * Function that takes a dp value...
 * ...and transforms it into px value
 */
fun Int.toPx(): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics
).toInt()

/**
 * To prevent adding new item decorations...
 * ...everytime an item is inserted inside a...
 * ...recyclerview adapter
 */
fun RecyclerView.removeItemDecorations() {
    while (this.itemDecorationCount > 0) {
        this.removeItemDecorationAt(0)
    }
}

/**
 * Function that takes a long...
 * ...and returns a formatted date...
 * ...like: "13 Mar 2023"
 */
fun Long.toFormattedDate(): String {
    var pattern = "dd MMM yyyy"
    val date = Date(this)
    val realCalendar = Calendar.getInstance()
    val fixedCalendar = Calendar.getInstance()
    fixedCalendar.time = date
    val realYear = realCalendar.get(Calendar.YEAR)
    val fixedYear = fixedCalendar.get(Calendar.YEAR)

    if (fixedYear == realYear) pattern = "dd MMM"

    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(date)
}

/**
 * Function that takes a long...
 * ...and returns a formatted duration...
 * ...like: "13 h 23 min"
 */
fun Long.toFormattedDuration(): String {
    val seconds = (this / 1000).toInt() % 60
    val minutes = (this / (1000 * 60) % 60)
    val hours = (this / (1000 * 60 * 60) ) // %24 removed for counting more than 24 hours

    var result = ""

    result = if(hours > 0)
        result.plus("$hours h $minutes min")
    else if(minutes > 0)
        result.plus("$minutes min")
    else
        result.plus("$seconds sec")

    return result
}

/**
 * Function that greets the user...
 * depending on his username
 */
fun displayGreeting(context: Context, username: String): String =
    when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 4..12 -> context.getString(
            R.string.greeting1,
            username
        )
        in 13..19 -> context.getString(
            R.string.greeting2,
            username
        )
        else -> context.getString(
            R.string.greeting3,
            username
        )
    }


