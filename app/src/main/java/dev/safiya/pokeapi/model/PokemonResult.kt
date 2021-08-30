package dev.safiya.pokeapi.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "PokemonResult",
    indices = arrayOf(Index(value = ["name", "id"], unique = true))
)
@Parcelize
data class PokemonResult(
    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "abilities")
    var abilities: List<Abilities>? = emptyList(),

    @ColumnInfo(name = "page")
    var page: Int,
    @ColumnInfo(name = "url")
    var url: String
) : Parcelable {
    @PrimaryKey(autoGenerate = false)
    var id: Int? = 0

}