import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test1.FriendRequest
import com.example.test1.FriendRequestStatus
import com.example.test1.R
import com.example.test1.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SearchUsersFragment : Fragment(), UserAdapter.OnFriendButtonClick {

    private lateinit var searchEditText: EditText
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private var userList: ArrayList<User> = ArrayList()
    private var filteredList: ArrayList<User> = ArrayList()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_users_fragments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        userRecyclerView = view.findViewById(R.id.userRecyclerView)

        // Set up RecyclerView
        userAdapter = UserAdapter(requireContext(), userList, this)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = userAdapter

        // Get a reference to the "users" node in the Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("user")

        // Add a ValueEventListener to fetch the user data
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    userList.add(user!!)
                }
                Log.d("SearchUsersFragment", "UserList: $userList") // Debug log

                // Don't notify the adapter here, as we don't want to initially display any users
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to read user data.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        searchEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Clear the search query and filter the user list to an empty list
                filterUserList("")
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                val searchQuery = s.toString()
                filterUserList(searchQuery)
            }
        })
    }

    private fun filterUserList(searchQuery: String) {
        filteredList.clear()
        if (searchQuery.isNotEmpty()) {
            for (user in userList) {
                if (user.name?.contains(searchQuery, ignoreCase = true) == true) {
                    filteredList.add(user)
                }
            }
        }
        Log.d("SearchUsersFragment", "FilteredList: $filteredList") // Debug log
        userAdapter.filterList(filteredList)
    }

    override fun onAddFriendClicked(position: Int) {
        val selectedUser = filteredList[position]
        if (filteredList.isEmpty() || position >= filteredList.size) {
            return
        }
        val senderUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val receiverUserId = selectedUser.uid!!

        val friendRequestRef = FirebaseDatabase.getInstance().getReference("friend_requests")
        friendRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val friendRequests =
                    dataSnapshot.children.mapNotNull { it.getValue(FriendRequest::class.java) }

                val existingFriendRequest = friendRequests.find {
                    it.senderId == senderUserId && it.receiverId == receiverUserId
                }

                if (existingFriendRequest != null) {
                    showToast("Friend request already sent to ${selectedUser.name}")
                } else {
                    val usersRef = FirebaseDatabase.getInstance().getReference("user")

                    // Retrieve sender's name
                    usersRef.child(senderUserId).child("name")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(senderDataSnapshot: DataSnapshot) {
                                val senderName = senderDataSnapshot.getValue(String::class.java)

                                // Retrieve receiver's name
                                usersRef.child(receiverUserId).child("name")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(receiverDataSnapshot: DataSnapshot) {
                                            val receiverName =
                                                receiverDataSnapshot.getValue(String::class.java)

                                            val friendRequestKey = FirebaseDatabase.getInstance()
                                                .getReference("friend_requests").push().key

                                            val receiverFriendRequestRef = FirebaseDatabase.getInstance().getReference("friend_requests").child(receiverUserId).child(senderUserId)
                                            val receiverFriendRequest = FriendRequest(
                                                receiverUserId,
                                                senderUserId,
                                                senderName,
                                                FriendRequestStatus.PENDING
                                            )
                                            receiverFriendRequestRef.setValue(receiverFriendRequest)

                                            showToast("Friend request sent to ${selectedUser.name}")

                                            // Check if the users are already friends
                                            if (selectedUser.friends?.get(senderUserId) == true &&
                                                selectedUser.friends?.get(receiverUserId) == true) {
                                                // Users are friends, change button text to "Friends" and disable the button
                                                userAdapter.setFriendButtonState(position, true)
                                            }
                                        }

                                        override fun onCancelled(receiverDatabaseError: DatabaseError) {
                                            showToast("Failed to retrieve receiver's name.")
                                        }
                                    })
                            }

                            override fun onCancelled(senderDatabaseError: DatabaseError) {
                                showToast("Failed to retrieve sender's name.")
                            }
                        })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("Failed to read friend requests.")
            }
        })
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext().applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}