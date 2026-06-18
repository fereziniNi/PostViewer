package br.edu.ifsp.scl.sc3044025.postviewer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.edu.ifsp.scl.sc3044025.postviewer.data.local.entity.LocalCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: LocalCommentEntity)

    @Query("SELECT * FROM local_comments WHERE postId = :postId ORDER BY id DESC")
    fun getCommentsByPostId(postId: Int): Flow<List<LocalCommentEntity>>
}