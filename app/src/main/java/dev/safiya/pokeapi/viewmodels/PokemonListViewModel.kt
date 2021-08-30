package dev.safiya.pokeapi.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.safiya.pokeapi.data.PokemonApplication
import dev.safiya.pokeapi.data.repositories.PokemonRepository
import dev.safiya.pokeapi.model.PokemonResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val application: PokemonApplication
) :
    ViewModel() {

    private var currentResult: Flow<PagingData<PokemonResult>>? = null

    fun getPokemons(
        searchString: String?,
        pageno: Int,
        sortBy: Int
    ): Flow<PagingData<PokemonResult>> {
        val newResult: Flow<PagingData<PokemonResult>> =
            pokemonRepository.getPokemon(
                searchString,
                pageno,
                sortBy,
                application.database.getPokemonDao(),
            )
                .cachedIn(viewModelScope)
        currentResult = newResult
        return newResult
    }
}