package com.example.koltincrudfirebase

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notes(
    var strId: String = "0",
    var strTitle: String? = null,
    var strDesc: String? = null,
    var strTanggal: String? = null,
): Parcelable