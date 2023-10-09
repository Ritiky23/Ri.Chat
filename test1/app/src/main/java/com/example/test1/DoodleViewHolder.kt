package com.example.test1

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class DoodleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val sender1: ImageView = itemView.findViewById(R.id.sender_image)
    val receiver1:ImageView=itemView.findViewById(R.id.receiver_image)
}
