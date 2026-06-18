package br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.scl.sc3044025.postviewer.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostListViewModel(private val postRepository: PostRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<PostListUiState>(PostListUiState.Loading)
    val uiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = PostListUiState.Loading
            try {
                val posts = postRepository.getPosts()
                _uiState.value = PostListUiState.Success(posts)
            } catch (e: Exception) {
                _uiState.value = PostListUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    class Factory(private val repository: PostRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PostListViewModel(repository) as T
    }
}