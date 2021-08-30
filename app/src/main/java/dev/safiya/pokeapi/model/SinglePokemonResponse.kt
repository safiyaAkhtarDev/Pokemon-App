package dev.safiya.pokeapi.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class SinglePokemonResponse(
    @ColumnInfo(name = "abilities")
    val abilities: List<Abilities>,
    @ColumnInfo(name = "sprites")
    val sprites: Sprites,
    @ColumnInfo(name = "stats")
    val stats: List<Stats>,
    @ColumnInfo(name = "height")
    val height: Int,
    @ColumnInfo(name = "weight")
    val weight: Int
) : Parcelable