package br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.scl.sc3044025.postviewer.data.repository.CommentRepository
import br.edu.ifsp.scl.sc3044025.postviewer.data.repository.PostRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostListViewModel(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostListUiState>(PostListUiState.Loading)
    val uiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = PostListUiState.Loading
            try {
                val postsDeferred = async { postRepository.getPosts() }
                val countsDeferred = async { commentRepository.getCommentCounts() }
                _uiState.value = PostListUiState.Success(
                    posts = postsDeferred.await(),
                    commentCounts = countsDeferred.await()
                )
            } catch (e: Exception) {
                _uiState.value = PostListUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    class Factory(
        private val postRepository: PostRepository,
        private val commentRepository: CommentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PostListViewModel(postRepository, commentRepository) as T
    }
}
