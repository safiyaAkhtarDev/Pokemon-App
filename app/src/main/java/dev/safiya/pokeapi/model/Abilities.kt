package dev.safiya.pokeapi.model

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Abilities(

    @SerializedName("ability")
    @ColumnInfo(name = "ability")
    val ability: Ability,

    ) : Parcelable