package br.edu.ifsp.scl.sc3044025.postviewer.data.remote.model

import com.google.gson.annotations.SerializedName

data class CommentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("postId") val postId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("body") val body: String
)