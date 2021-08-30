package dev.safiya.pokeapi.data.repositories

import PokemonDataSource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dev.safiya.pokeapi.api.PokemonApi
import dev.safiya.pokeapi.data.database.PokemonDao
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository used to access data being loaded from network call
 */

@Singleton
class PokemonRepository @Inject constructor(
    private val pokemonApi: PokemonApi,

    ) : BaseRepository() {

    fun getPokemon(searchString: String?, pageno: Int, sortBy: Int, pokemonDao: PokemonDao?=null) = Pager(
        config = PagingConfig(enablePlaceholders = false, pageSize = 20),
        pagingSourceFactory = {
            PokemonDataSource(pokemonApi, searchString, pokemonDao!!, pageno, sortBy)
        }
    ).flow
    suspend fun getSinglePokemon(id: Int) = safeApiCall {
        pokemonApi.getSinglePokemon(id)

    }
}