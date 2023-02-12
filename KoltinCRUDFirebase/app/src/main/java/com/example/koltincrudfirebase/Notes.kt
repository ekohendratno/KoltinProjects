package com.example.koltincrudfirebase

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notes(
    var title: String? = null,
    var desc: String? = null,
    var tanggal: String? = null,
): Parcelable