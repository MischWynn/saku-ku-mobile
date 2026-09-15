package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

// Field persis NotificationEntity backend - "isRead" dicross-check langsung ke compiled
// class (getIsRead(), bukan isRead()) karena field-nya Boolean boxed bukan boolean primitif -
// itu bikin Jackson tetep pake nama "isRead", bukan "read" (yang biasanya jadi konvensi kalau
// primitif). Jangan diubah tanpa cross-check ulang.
@Serializable
data class NotificationResponse(
    val id: String,
    val judul: String,
    val pesan: String,
    val isRead: Boolean,
    val createdAt: String? = null
)
