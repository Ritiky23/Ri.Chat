import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test1.ChatActivity
import com.example.test1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendsChatFragment : Fragment(), FriendAdapter.OnUserClickListener {
    private lateinit var friendRecyclerView: RecyclerView
    private var friendAdapter: FriendAdapter? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private val userList: ArrayList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_friends_chat_fragments, container, false)
        friendRecyclerView = view.findViewById(R.id.FriendRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        friendAdapter = FriendAdapter(requireContext(), userList, this)
        friendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendRecyclerView.adapter = friendAdapter

        firebaseAuth = FirebaseAuth.getInstance()
        // Retrieve user list from Firebase Realtime Database
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        val friendsRef = FirebaseDatabase.getInstance().getReference("user")
            .child(currentUserID!!)
            .child("friends")

        friendsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (friendSnapshot in dataSnapshot.children) {
                    val friendID = friendSnapshot.key
                    friendID?.let {
                        userList.add(friendID)
                    }
                }
                friendAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error case here
            }
        })
    }

    override fun onUserClick(friendID: String, friendName: String) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("friendID", friendID!!)
        intent.putExtra("name", friendName)
        startActivity(intent)
    }
}
