package com.example.koltin1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize



@Parcelize
data class Notes(val title: String, val desc: String): Parcelable