package com.example.test1

import FriendRequestAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendRequestActivity : AppCompatActivity(), FriendRequestAdapter.OnFriendRequestActionListener {

    private lateinit var friendRequestRecyclerView: RecyclerView
    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private var friendRequestList: ArrayList<FriendRequest> = ArrayList()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        friendRequestRecyclerView = findViewById(R.id.friendRequestRecyclerView)

        // Set up RecyclerView
        friendRequestAdapter = FriendRequestAdapter(friendRequestList, this)
        friendRequestRecyclerView.layoutManager = LinearLayoutManager(this)
        friendRequestRecyclerView.adapter = friendRequestAdapter

        // Get a reference to the "friend_requests" node of the current user in the Firebase Realtime Database
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("friend_requests")

        // Add a ValueEventListener to fetch the friend request data
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                friendRequestList.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val userID = userSnapshot.key
                    for (friendRequestSnapshot in userSnapshot.children) {
                        val friendRequest = friendRequestSnapshot.getValue(FriendRequest::class.java)
                        friendRequest?.let {
                            if (friendRequest.senderId == currentUserID || friendRequest.receiverId == currentUserID) {
                                if (friendRequest.status != FriendRequestStatus.ACCEPTED) {
                                    friendRequestList.add(it)
                                }
                            }
                        }
                    }
                }
                Log.d("FriendRequestActivity", "FriendRequestList: $friendRequestList") // Debug log
                friendRequestAdapter.notifyDataSetChanged()

                // Check if friendRequestList is empty and handle the UI accordingly
                if (friendRequestList.isEmpty()) {
                    // Display a message or handle the UI for an empty friend request list
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@FriendRequestActivity, "Failed to read friend requests.", Toast.LENGTH_SHORT).show()
                // Handle the error in an appropriate way
            }
        })
    }

    override fun onAcceptFriendRequest(friendRequest: FriendRequest) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

        // Show a confirmation dialog to the user before accepting the friend request
        // If the user confirms, proceed with the following code

        // Update the friend request status in the database
        val senderUserID = friendRequest.senderId
        val requestRef = FirebaseDatabase.getInstance().getReference("friend_requests")
        val senderRequestRef = requestRef.child(senderUserID).child(currentUserID)
        val receiverRequestRef = requestRef.child(currentUserID).child(senderUserID)

        friendRequest.status = FriendRequestStatus.ACCEPTED
        senderRequestRef.setValue(friendRequest)
        receiverRequestRef.setValue(friendRequest)

        // Remove the friend request from the list
        friendRequestList.remove(friendRequest)
        friendRequestAdapter.notifyDataSetChanged()

        // Show a toast message or any UI feedback to indicate the friend request has been accepted
        Toast.makeText(this, "Friend request accepted from ${friendRequest.senderName}", Toast.LENGTH_SHORT).show()

        // Add the accepted friend to the friend list of both users
        val currentUserFriendsRef = FirebaseDatabase.getInstance().getReference("user")
            .child(currentUserID)
            .child("friends")
        val senderUserFriendsRef = FirebaseDatabase.getInstance().getReference("user")
            .child(senderUserID)
            .child("friends")

        currentUserFriendsRef.child(senderUserID).setValue(true)
        senderUserFriendsRef.child(currentUserID).setValue(true)
    }

    override fun onDeleteFriendRequest(friendRequest: FriendRequest) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

        // Show a confirmation dialog to the user before deleting the friend request
        // If the user confirms, proceed with the following code

        // Remove the friend request from the database
        val senderUserID = friendRequest.senderId
        val requestRef = FirebaseDatabase.getInstance().getReference("friend_requests")
        val senderRequestRef = requestRef.child(senderUserID).child(currentUserID)
        val receiverRequestRef = requestRef.child(currentUserID).child(senderUserID)

        senderRequestRef.removeValue()
        receiverRequestRef.removeValue()

        // Remove the friend request from the list
        friendRequestList.remove(friendRequest)
        friendRequestAdapter.notifyDataSetChanged()

        // Show a toast message or any UI feedback to indicate the friend request has been deleted
        Toast.makeText(this, "Friend request deleted from ${friendRequest.senderName}", Toast.LENGTH_SHORT).show()
    }
}
