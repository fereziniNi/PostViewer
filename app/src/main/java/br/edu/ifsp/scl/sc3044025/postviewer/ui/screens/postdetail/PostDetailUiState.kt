package br.edu.ifsp.scl.sc3044025.postviewer.ui.screens.postdetail

import br.edu.ifsp.scl.sc3044025.postviewer.data.local.entity.LocalCommentEntity
import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model.CommentDto

data class PostDetailUiState(
    val isLoading: Boolean = true,
    val apiComments: List<CommentDto> = emptyList(),
    val localComments: List<LocalCommentEntity> = emptyList(),
    val errorMessage: String? = null
)