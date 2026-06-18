package br.edu.ifsp.scl.sc3044025.postviewer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_comments")
data class LocalCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Int,
    val body: String
)
