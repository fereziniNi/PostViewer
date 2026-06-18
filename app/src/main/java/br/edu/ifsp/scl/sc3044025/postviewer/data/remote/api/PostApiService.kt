package br.edu.ifsp.scl.sc3044025.postviewer.data.remote.api

import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model.CommentDto
import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model.PostDto
import retrofit2.http.GET
import retrofit2.http.Path

interface PostApiService {

    @GET("posts")
    suspend fun getPosts(): List<PostDto>

    @GET("posts/{id}/comments")
    suspend fun getCommentsByPostId(@Path("id") postId: Int): List<CommentDto>
}