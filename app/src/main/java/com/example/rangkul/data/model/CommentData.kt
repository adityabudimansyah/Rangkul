package com.example.rangkul.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@kotlinx.parcelize.Parcelize
data class CommentData (
        @DocumentId
        var commentId: String = "",
        val commentedBy: String = "", //mengubah commentedBy menjadi userId untuk skripsi
        @ServerTimestamp
        val commentedAt: Date = Date(),
        val comment: String = "",
        val userName: String = "", //mengubah userName menjadi commentedBy untuk skripsi
        val profilePicture: String? = null,
        val userBadge: String = "",
        val postId: String = ""
): Parcelable
