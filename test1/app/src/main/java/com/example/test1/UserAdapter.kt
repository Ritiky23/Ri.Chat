import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test1.FriendRequest
import com.example.test1.FriendRequestStatus
import com.example.test1.R
import com.example.test1.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAdapter(
    private val context: Context,
    private var userList: List<User>,
    private val onFriendButtonClick: OnFriendButtonClick,
    private var filteredList: ArrayList<User> = ArrayList()
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.usernameText)
        val addFriendButton: Button = itemView.findViewById(R.id.addFriendButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        holder.usernameText.text = user.name

        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val currentUserFriendRequestsRef =
            FirebaseDatabase.getInstance().getReference("friend_requests")
                .child(currentUserID)

        currentUserFriendRequestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val friendRequests =
                    dataSnapshot.children.mapNotNull { it.getValue(FriendRequest::class.java) }
                val isFriend = friendRequests.any { friendRequest ->
                    friendRequest.receiverId == user.uid && friendRequest.status == FriendRequestStatus.ACCEPTED
                }

                val isFriendRequestSent = friendRequests.any { friendRequest ->
                    friendRequest.receiverId == user.uid && friendRequest.status == FriendRequestStatus.PENDING
                }

                if (isFriend) {
                    holder.addFriendButton.text = context.getString(R.string.friends)
                    holder.addFriendButton.isEnabled = false
                } else if (isFriendRequestSent) {
                    holder.addFriendButton.text = context.getString(R.string.REQUESTED)
                    holder.addFriendButton.isEnabled = false
                } else {
                    holder.addFriendButton.text = context.getString(R.string.add_friend)
                    holder.addFriendButton.isEnabled = true
                }

                // Check if the user is a friend and disable the button accordingly
                val isCurrentUserFriend = friendRequests.any { friendRequest ->
                    friendRequest.receiverId == currentUserID && friendRequest.status == FriendRequestStatus.ACCEPTED
                }

                if (isFriend || isCurrentUserFriend) {
                    // Users are friends, disable the button
                    holder.addFriendButton.isEnabled = false
                } else {
                    // Users are not friends, enable the button
                    holder.addFriendButton.isEnabled = true
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error case here
            }
        })

        holder.addFriendButton.setOnClickListener {
            // Call the interface method to handle the button click
            onFriendButtonClick.onAddFriendClicked(position)
            // Disable the button and update UI immediately
            holder.addFriendButton.isEnabled = false
            holder.addFriendButton.text = context.getString(R.string.REQUESTED)
        }
    }

    fun setFriendButtonState(position: Int, isFriends: Boolean) {
        if (position >= 0 && position < userList.size) {
            val user = userList[position]
            val friendButtonText = if (isFriends) {
                context.getString(R.string.friends)
            } else {
                context.getString(R.string.add_friend)
            }
            user.friends = mapOf(user.uid!! to isFriends) // Update the user's friends map
            notifyItemChanged(position)
        }
    }


    override fun getItemCount(): Int {
        return userList.size
    }

    fun filterList(filteredList: ArrayList<User>) {
        userList = filteredList
        notifyDataSetChanged()
    }

    interface OnFriendButtonClick {
        fun onAddFriendClicked(position: Int)
    }
}
