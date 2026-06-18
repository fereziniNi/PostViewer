package br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postlist

import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model.PostDto

sealed interface PostListUiState {
    data object Loading : PostListUiState
    data class Success(val posts: List<PostDto>) : PostListUiState
    data class Error(val message: String) : PostListUiState
}