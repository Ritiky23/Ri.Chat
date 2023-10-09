package com.example.test1

import com.google.firebase.database.PropertyName

data class FriendRequest(
    @PropertyName("receiverId")
    val receiverId: String = "",

    @PropertyName("senderId")
    val senderId: String = "",

    @PropertyName("senderName")
    val senderName: String? = null,

    @PropertyName("status")
    var status: FriendRequestStatus = FriendRequestStatus.PENDING
)

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}
