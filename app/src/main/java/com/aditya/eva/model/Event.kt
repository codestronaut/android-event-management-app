package com.aditya.eva.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Event(
    var event_date: String? = null,
    var event_description: String? = null,
    var event_name: String? = null,
    var event_organizer: String? = null,
    var event_place: String? = null,
    var event_price: String? = null,
    var image_url: String? = null,
    var is_event_pain: Boolean? = false
) : Parcelable