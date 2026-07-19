package com.example.sonor.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonor.domain.model.Song
import com.example.sonor.domain.repository.SongRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // State for Recent Searches from DB
    val recentSearches = songRepository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State for Discover Content (Lark Player style: Recently Played / Top)
    val recentlyPlayed = songRepository.getRecentlyPlayed()
        .map { it.take(6) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(FlowPreview::class)
    val filteredSongs = _searchQuery
        .debounce(300L)
        .combine(songRepository.getAllSongs()) { query, allSongs ->
            if (query.isBlank()) {
                emptyList()
            } else {
                allSongs.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSearchSubmitted(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                songRepository.addRecentSearch(query)
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun onSongClicked(song: Song) {
        viewModelScope.launch {
            songRepository.addToHistory(song.id)
            songRepository.incrementPlayCount(song.id)
            if (_searchQuery.value.isNotBlank()) {
                songRepository.addRecentSearch(_searchQuery.value)
            }
        }
    }
}
