package dev.safiya.pokeapi.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.safiya.pokeapi.data.repositories.PokemonRepository
import dev.safiya.pokeapi.utils.NetworkResource
import dev.safiya.pokeapi.utils.extractId
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


@HiltViewModel
class PokemonStatsViewModel @Inject constructor(private val pokemonRepository: PokemonRepository) :
    ViewModel() {

    suspend fun getSinglePokemon(url: String) = flow {
        val id = url.extractId()
        emit(NetworkResource.Loading)
        emit(pokemonRepository.getSinglePokemon(id))
    }

}