package dev.safiya.pokeapi.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ability(

    @SerializedName("name")
    @ColumnInfo(name = "name")
    val name: String

) : Parcelable