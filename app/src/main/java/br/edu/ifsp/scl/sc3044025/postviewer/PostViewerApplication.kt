package br.edu.ifsp.scl.sc3044025.postviewer

import android.app.Application
import br.edu.ifsp.scl.sc3044025.postviewer.data.local.AppDatabase
import br.edu.ifsp.scl.sc3044025.postviewer.data.remote.RetrofitInstance
import br.edu.ifsp.scl.sc3044025.postviewer.data.repository.CommentRepository
import br.edu.ifsp.scl.sc3044025.postviewer.data.repository.PostRepository

class PostViewerApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val postRepository by lazy { PostRepository(RetrofitInstance.apiService) }
    val commentRepository by lazy {
        CommentRepository(RetrofitInstance.apiService, database.localCommentDao())
    }
}