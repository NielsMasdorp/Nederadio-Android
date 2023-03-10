package com.nielsmasdorp.nederadio.ui.search

import androidx.lifecycle.*
import com.nielsmasdorp.nederadio.domain.stream.*
import kotlinx.coroutines.flow.*

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class SearchViewModel(
    getAllStreams: GetAllStreams,
    private val streamManager: StreamManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchedStreams: LiveData<List<Stream>> =
        getAllStreams.streams.combine(searchQuery) { streams, query ->
            when (streams) {
                is Streams.Success -> streams.streams.filter {
                    it.title.contains(
                        query,
                        ignoreCase = true
                    )
                }
                else -> emptyList()
            }
        }.asLiveData()


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStreamPicked(id: String) = streamManager.streamPicked(id)
}