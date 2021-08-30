package dev.safiya.pokeapi.data.database

import androidx.room.*
import dev.safiya.pokeapi.model.SinglePokemonResponse

@Dao
interface SinglePokemonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SinglePokemonResponse)

    @Delete
    suspend fun delete(item: SinglePokemonResponse)

    @Query("SELECT * FROM SinglePokemonResponse where id=:id")
    suspend fun getPokemon(id: Int): List<SinglePokemonResponse>

}