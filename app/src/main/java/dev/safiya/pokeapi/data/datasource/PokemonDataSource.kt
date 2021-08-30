import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.safiya.pokeapi.api.PokemonApi
import dev.safiya.pokeapi.data.database.PokemonDao
import dev.safiya.pokeapi.model.PokemonResult
import dev.safiya.pokeapi.utils.LOAD_SIZE
import dev.safiya.pokeapi.utils.MAX_BASE_STATE
import dev.safiya.pokeapi.utils.SEARCH_LOAD_SIZE
import dev.safiya.pokeapi.utils.STARTING_OFFSET_INDEX
import java.io.IOException

/**
 * Paging 3 library which is under Android Jetpack. Used to paginate data. Here I am paginating the data using the pokeapi pagination
 */
class PokemonDataSource(
    private val pokemonApi: PokemonApi,
    private val searchString: String?,
    private val pokemonDao: PokemonDao,
    private var pageno: Int,
    private var sortBy: Int,
    ) :
    PagingSource<Int, PokemonResult>() {

    var pokemonList: List<PokemonResult>? = null
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonResult> {
        var offset = params.key ?: STARTING_OFFSET_INDEX
        var loadSize = if (searchString == null) LOAD_SIZE else SEARCH_LOAD_SIZE

        return try {

            if (pageno == 0 && pokemonDao.getPokemonCount() != null
                && pokemonDao.getPokemonCount() <= MAX_BASE_STATE
            ) {
                //fetch new pokemon  everytime App is opened
                offset = pokemonDao.getPokemonCount()
                if (pokemonDao.getPokemonPage() != null)
                    pageno = pokemonDao.getPokemonPage()
                else
                    pageno = 1
                val data = pokemonApi.getPokemons(loadSize, offset)
                val filteredData = data.results
                // for every single pokemon abilities are not available ein the same api so we
                //  need to call another api for every pokemon,
//                    it would be easier if endpoint gave abilities and pokemon details in the same API
                filteredData.forEach { pokemon ->
                    pokemon.page = pageno
                    pokemon.abilities = pokemonApi.getSinglePokemon(
                        pokemon.url.split("/".toRegex()).dropLast(1).last().toInt()
                    ).abilities!!
                    pokemon.id = pokemon.url.split("/".toRegex()).dropLast(1).last().toInt()
                }

                for (i: PokemonResult in filteredData) {
                    // insert pokemon in db
                    pokemonDao.insert(i)
                }

            }

            offset = params.key ?: STARTING_OFFSET_INDEX

            // get all pokemon stored in database on current page number
            pokemonList = pokemonDao.getAllPokemon(pageno)

            //if search string isn't null filter the pokemon based on what user searched.
            if (searchString != null) {
                pokemonList = pokemonDao.getSearchedPokemon(searchString)
                // when search string is passed, fetch data based on the string
                val data = pokemonApi.getPokemons(loadSize, offset)
                if (pageno == 0) {
                    pageno = pageno.inc()
                }
                val filteredData =
                    data.results.filter { it.name.contains(searchString, true) }
                // for every single pokemon abilities are not available ein the same api so we
                //  need to call another api for every pokemon,
//                    it would be easier if endpoint gave abilities and pokemon details in the same API
                filteredData.forEach { pokemon ->
                    pokemon.page = pageno
                    pokemon.abilities = pokemonApi.getSinglePokemon(
                        pokemon.url.split("/".toRegex()).dropLast(1).last().toInt()
                    ).abilities!!
                    pokemon.id = pokemon.url.split("/".toRegex()).dropLast(1).last().toInt()
                }
                LoadResult.Page(
                    data = filteredData,
                    prevKey = if (offset == STARTING_OFFSET_INDEX) null else offset - loadSize,
                    nextKey = if (data.next == null) null else offset + loadSize
                )
            }
            // There is data in db for current page and search string was null,
//            so we will normally load data from database
            else if (pokemonList != null && pokemonList?.size!! > 0) {
                if (sortBy == 1) {
                    // sort 1 to N
                    pokemonList = pokemonDao.get1toNPokemon(pageno)
                    pageno = pageno.inc()
                } else if (sortBy == 2) {
                    //sort A to Z and page 1 because only once we want to call this
                    pokemonList = pokemonDao.getAtoZPokemon()
                    pageno = pageno.inc()
                }

                LoadResult.Page(
                    data = pokemonList!!,
                    prevKey = if (offset == STARTING_OFFSET_INDEX) null else offset - loadSize,
                    nextKey = (pageno.dec() * LOAD_SIZE) + loadSize + loadSize
                )
            }
            // if there was no data for current page in db then we need to call api
            else {
                if (offset <= MAX_BASE_STATE) {
                    // fetching data from the api till it reaches max load capacity (i.e. 300)
                    val data = pokemonApi.getPokemons(loadSize, offset)
                    if (pageno == 0) {
                        pageno = pageno.inc()
                    }

                    // for every single pokemon abilities are not available ein the same api so we
                    //  need to call another api for every pokemon,
//                    it would be easier if endpoint gave abilities and pokemon details in the same API
                    val filteredData = if (searchString != null) {
                        data.results.forEach { pokemon ->
                            pokemon.page = pageno
                            pokemon.abilities = pokemonApi.getSinglePokemon(
                                pokemon.url.split("/".toRegex()).dropLast(1).last().toInt()
                            ).abilities!!
                            pokemon.id = pokemon.url.split("/".toRegex()).dropLast(1).last().toInt()
                        }
                        data.results
                    } else {
                        data.results.forEach { pokemon ->
                            pokemon.page = pageno
                            pokemon.abilities = pokemonApi.getSinglePokemon(
                                pokemon.url.split("/".toRegex()).dropLast(1).last().toInt()
                            ).abilities!!
                            pokemon.id = pokemon.url.split("/".toRegex()).dropLast(1).last().toInt()
                        }
                        data.results
                    }
                    pageno = pageno.inc()

                    for (i: PokemonResult in filteredData) {
                        // insert pokemon in db
                        pokemonDao.insert(i)
                    }

                    LoadResult.Page(
                        data = filteredData,
                        prevKey = if (offset == STARTING_OFFSET_INDEX) null else offset - loadSize,
                        nextKey = if (data.next == null) null else (pageno.dec() * LOAD_SIZE) + loadSize + loadSize
                    )
                } else {
                    LoadResult.Page(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = null
                    )
                }

            }


            //next offset = last offset + loadsize, returning null is telling the paging 3 that there is no more to load and should stop

        } catch (t: Throwable) {
            var exception = t
            t.printStackTrace()
            if (t is IOException) {
                exception = IOException("Please check internet connection")
            }
            LoadResult.Error(exception)
        }


    }


    override fun getRefreshKey(state: PagingState<Int, PokemonResult>): Int? {

        return state.anchorPosition

    }
}