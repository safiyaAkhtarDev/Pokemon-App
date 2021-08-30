package dev.safiya.pokeapi.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import dev.safiya.pokeapi.R
import dev.safiya.pokeapi.adapters.LoadingStateAdapter
import dev.safiya.pokeapi.adapters.PokemonAdapter
import dev.safiya.pokeapi.databinding.FragmentPokemonListBinding
import dev.safiya.pokeapi.model.PokemonResult
import dev.safiya.pokeapi.utils.PRODUCT_VIEW_TYPE
import dev.safiya.pokeapi.utils.toast
import dev.safiya.pokeapi.utils.toggle
import dev.safiya.pokeapi.viewmodels.PokemonListViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.Exception


@AndroidEntryPoint
@SuppressLint("ClickableViewAccessibility")
class PokemonListFragment : Fragment(R.layout.fragment_pokemon_list) {

    private var hasInitiatedInitialCall = false
    private lateinit var binding: FragmentPokemonListBinding
    private val viewModel: PokemonListViewModel by viewModels()
    private var job: Job? = null
    private var sortBy: Int? = 1

    private var hasUserSearched = false

    //adapter with higher order function passed which is called on onclick on adapter
    private val adapter =
        PokemonAdapter { pokemonResult: PokemonResult, dominantColor: Int, picture: String? ->
            navigate(
                pokemonResult,
                dominantColor,
                picture
            )
        }

    //navigating to stats fragment passing the pokemon and the dominant color
    private fun navigate(pokemonResult: PokemonResult, dominantColor: Int, picture: String?) {
        binding.root.findNavController()
            .navigate(
                PokemonListFragmentDirections.toPokemonStatsFragment(
                    pokemonResult,
                    dominantColor, picture
                )
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPokemonListBinding.bind(view)

        setAdapter()
        setRefresh()
        setSearchView()
        setsortTabs()


        binding.scrollUp.setOnClickListener {
            lifecycleScope.launch {
                binding.pokemonList.scrollToPosition(0)
                delay(100)
                binding.scrollUp.toggle(false)
            }
        }

    }

    private fun setsortTabs() {

        binding.tablayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                try {
                    // we can choose to keep the state of recycler view or scroll to the top most position
                    binding.pokemonList.scrollToPosition(1)

                    // for smooth scrolling to top position when tab is selected
//                    binding.pokemonList.smoothScrollToPosition(1)
                    if (tab.position == 1) {
//                        sort 1 to N
                        binding.searchView.setText("")
                        sortBy = 1
                        startFetchingPokemon(null, true)
                    } else if (tab.position == 2) {
//                        sort A to Z
                        binding.searchView.setText("")
                        sortBy = 2
                        startFetchingPokemon(null, true)
                    } else {
                        //default showing latest pokemons
                        binding.searchView.setText("")
                        sortBy = 1
                        startFetchingPokemon(null, true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            startFetchingPokemon(null, true)

            binding.searchView.apply {
                text = null
                isFocusable = false
            }

            hideSoftKeyboard()
        }
    }

    private fun setSearchView() {
        binding.searchView.setOnTouchListener { v, _ ->
            v.isFocusableInTouchMode = true
            false
        }
        binding.searchView.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hasUserSearched = true
                binding.scrollUp.toggle(false)
                performSearch(binding.searchView.text.toString().trim())
                return@OnEditorActionListener true
            }
            false
        })

        binding.searchView.addTextChangedListener {
            // Search is blank
            if (it.toString().isEmpty() && hasUserSearched) {
                startFetchingPokemon(null, true)
                hideSoftKeyboard()
                hasUserSearched = false
            }
        }

    }


    private fun startFetchingPokemon(searchString: String?, shouldSubmitEmpty: Boolean) {
        //collecting flow then setting to adapter
        job?.cancel()
        job = lifecycleScope.launch {
            if (shouldSubmitEmpty) {
                adapter.submitData(PagingData.empty())
                viewModel.getPokemons(searchString, 1, sortBy!!)
                    .collectLatest {
                        adapter.submitData(it)
                    }
            } else {
                viewModel.getPokemons(searchString, 0, sortBy!!)
                    .collectLatest {
                        adapter.submitData(it)
                    }
            }
        }
    }

    private fun performSearch(searchString: String) {
        hideSoftKeyboard()
        if (searchString.isEmpty()) {
            requireContext().toast("Search cannot be empty")
            return
        }
        startFetchingPokemon(searchString, true)
    }

    private fun hideSoftKeyboard() {
        val view = requireActivity().currentFocus

        view?.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }


    }

    private fun setAdapter() {

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = adapter.getItemViewType(position)
                return if (viewType == PRODUCT_VIEW_TYPE) 1
                else 2
            }
        }
        binding.pokemonList.layoutManager = gridLayoutManager
        binding.pokemonList.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter { retry() }
        )

        binding.pokemonList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val scrolledPosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()

                if (scrolledPosition != null) {
                    if (scrolledPosition >= 1) {
                        binding.scrollUp.toggle(true)
                    } else {
                        binding.scrollUp.toggle(false)
                    }
                }

            }
        })

        if (!hasInitiatedInitialCall) startFetchingPokemon(null, false); hasInitiatedInitialCall =
            true

        //the progress will only show when the adapter is refreshing and its empty
        adapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading && adapter.snapshot().isEmpty()
            ) {
                binding.progressCircular.isVisible = true
                binding.textError.isVisible = false

            } else {
                binding.progressCircular.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false

                //if there is error a textview will show the error encountered.

                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }

                if (adapter.snapshot().isEmpty()) {
                    error?.let {
                        binding.textError.visibility = View.VISIBLE
                        binding.textError.setOnClickListener {
                            adapter.retry()
                        }
                    }

                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        binding.searchView.isFocusable = false
    }

    override fun onResume() {
        super.onResume()
        //setting the status bar color back
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.green)
    }

    private fun retry() {
        adapter.retry()
    }


}