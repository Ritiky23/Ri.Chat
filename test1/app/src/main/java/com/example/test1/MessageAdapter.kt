import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test1.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    val context: Context,
    val messageList: ArrayList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2
    private val MESSAGE_TYPE_IMAGE_SENT = 3
    private val MESSAGE_TYPE_IMAGE_RECEIVE = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_SENT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.sentchat, parent, false)
                SentViewHolder(view)
            }
            ITEM_RECEIVE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.recievechat, parent, false)
                ReceiveViewHolder(view)
            }
            MESSAGE_TYPE_IMAGE_SENT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_doodle_sent, parent, false)
                ImageSentViewHolder(view)
            }
            MESSAGE_TYPE_IMAGE_RECEIVE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_doodle_receive, parent, false)
                ImageReceiveViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        when (holder) {
            is SentViewHolder -> {
                holder.sendMessage.text = currentMessage.message
            }
            is ReceiveViewHolder -> {
                holder.receiveMessage.text = currentMessage.message
            }
            is ImageSentViewHolder -> {
                holder.bindBase64Image(currentMessage.drawingImageUrl, currentMessage.senderId)
            }
            is ImageReceiveViewHolder -> {
                holder.bindBase64Image(currentMessage.drawingImageUrl, currentMessage.receiverId)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return when {
            currentMessage.drawingImageUrl != null -> {
                if (FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId) {
                    MESSAGE_TYPE_IMAGE_SENT
                } else {
                    MESSAGE_TYPE_IMAGE_RECEIVE
                }
            }
            FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId -> ITEM_SENT
            else -> ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sendMessage: TextView = itemView.findViewById(R.id.text_sent)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.text_receive)
    }

    inner class ImageSentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderImageView: ImageView = itemView.findViewById(R.id.sender_image)

        fun bindBase64Image(base64Image: String?, senderId: String?) {
            val bitmap = decodeBase64Image(base64Image)
            if (bitmap != null) {
                if (senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                    senderImageView.setImageBitmap(bitmap)
                    senderImageView.visibility = View.VISIBLE
                } else {
                    senderImageView.setImageResource(R.drawable.placeholder)
                    senderImageView.visibility = View.GONE
                }
            } else {
                senderImageView.setImageResource(R.drawable.placeholder)
                senderImageView.visibility = View.VISIBLE
            }
        }

        private fun decodeBase64Image(base64Image: String?): Bitmap? {
            if (base64Image != null) {
                val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            }
            return null
        }
    }

    inner class ImageReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiverImageView: ImageView = itemView.findViewById(R.id.receiver_image)

        fun bindBase64Image(base64Image: String?, senderId: String?) {

            val bitmap = decodeBase64Image(base64Image)
            if (bitmap != null) {
                if (senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                    receiverImageView.setImageBitmap(bitmap)
                    receiverImageView.visibility = View.VISIBLE
                } else {
                    receiverImageView.setImageBitmap(null) // Clear the image if it's the receiver's own image
                    receiverImageView.visibility = View.GONE
                }
            } else {
                receiverImageView.setImageResource(R.drawable.placeholder)
                receiverImageView.visibility = View.VISIBLE
            }
        }

        private fun decodeBase64Image(base64Image: String?): Bitmap? {
            if (base64Image != null) {
                val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            }
            return null
        }
    }


    private fun isValidUrl(url: String?): Boolean {
        return url != null && url.isNotEmpty() && Patterns.WEB_URL.matcher(url).matches()
    }
}
