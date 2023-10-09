import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test1.R
import com.example.test1.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class FriendAdapter(
    private val context: Context,
    private val userList: List<String>,
    private val onUserClickListener: OnUserClickListener
) : RecyclerView.Adapter<FriendAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_friend_list, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val friendID = userList[position]
        // Retrieve friend's name from the Firebase Realtime Database based on friendID
        val friendNameRef = FirebaseDatabase.getInstance().getReference("user").child(friendID)
        friendNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val friendName = dataSnapshot.getValue(User::class.java)
                holder.senderNameTextView.text=friendName?.name
                friendName?.profilePictureUrl?.let { url ->
                    Picasso.get().load(url).into(holder.image)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error case here
            }
        })

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            Log.d("FriendAdapter", "Clicked on friendID: $friendID")
            onUserClickListener.onUserClick(friendID,friendNameRef.toString())
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderNameTextView: TextView = itemView.findViewById(R.id.usernameText)
        val image:ImageView=itemView.findViewById(R.id.imageView)
    }

    interface OnUserClickListener {
        fun onUserClick(friendID: String,friendName:String)
    }
}