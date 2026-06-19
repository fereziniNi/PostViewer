package br.edu.ifsp.scl.sc3044025.postviewer.data.repository

import br.edu.ifsp.scl.sc3044025.postviewer.data.local.dao.LocalCommentDao
import br.edu.ifsp.scl.sc3044025.postviewer.data.local.entity.LocalCommentEntity
import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.api.PostApiService
import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model.CommentDto
import kotlinx.coroutines.flow.Flow

class CommentRepository(
    private val apiService: PostApiService,
    private val localCommentDao: LocalCommentDao
) {

    suspend fun getApiComments(postId: Int): List<CommentDto> =
        apiService.getCommentsByPostId(postId)

    fun getLocalComments(postId: Int): Flow<List<LocalCommentEntity>> =
        localCommentDao.getCommentsByPostId(postId)

    suspend fun addLocalComment(postId: Int, body: String) {
        localCommentDao.insert(LocalCommentEntity(postId = postId, body = body))
    }

    suspend fun getCommentCounts(): Map<Int, Int> =
        apiService.getAllComments().groupingBy { it.postId }.eachCount()
}