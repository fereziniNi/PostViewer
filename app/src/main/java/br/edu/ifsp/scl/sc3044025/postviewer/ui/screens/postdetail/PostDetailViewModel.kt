package br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.scl.sc3044025.postviewer.data.repository.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val postId: Int,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        loadApiComments()
    }

    private fun loadApiComments() {
        viewModelScope.launch {
            try {
                val comments = commentRepository.getApiComments(postId)
                _uiState.update { it.copy(isLoading = false, apiComments = comments) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Erro desconhecido")
                }
            }
        }
    }

    class Factory(
        private val postId: Int,
        private val repository: CommentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PostDetailViewModel(postId, repository) as T
    }
}