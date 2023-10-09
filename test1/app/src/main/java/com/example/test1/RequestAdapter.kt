import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test1.FriendRequest
import com.example.test1.R

class FriendRequestAdapter(
    private val friendRequestList: List<FriendRequest>,
    private val listener: OnFriendRequestActionListener
) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    interface OnFriendRequestActionListener {
        fun onAcceptFriendRequest(friendRequest: FriendRequest)
        fun onDeleteFriendRequest(friendRequest: FriendRequest)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friendRequest = friendRequestList[position]
        holder.senderNameTextView.text = friendRequest.senderName

        holder.acceptButton.setOnClickListener {
            listener.onAcceptFriendRequest(friendRequest)
        }

        holder.deleteButton.setOnClickListener {
            listener.onDeleteFriendRequest(friendRequest)
        }
    }

    override fun getItemCount(): Int {
        return friendRequestList.size
    }
}
