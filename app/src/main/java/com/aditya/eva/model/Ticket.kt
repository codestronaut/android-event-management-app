package com.aditya.eva.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ticket(
    var ticket_buyer: String? = null,
    var ticket_name: String? = null,
    var ticket_price: String? = null,
    var ticket_qr_code: String? = null
) : Parcelable