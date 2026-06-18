package br.edu.ifsp.scl.sc3044025.postviewer.data.repository

import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.api.PostApiService
import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model.PostDto

class PostRepository(private val apiService: PostApiService) {
    suspend fun getPosts(): List<PostDto> = apiService.getPosts()
}