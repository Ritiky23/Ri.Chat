package com.example.test1



import FriendsChatFragment
import HomePagerAdapter
import ProfileFragment
import SearchUsersFragment
import UserAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class homeactivity : AppCompatActivity(), UserAdapter.OnFriendButtonClick {
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserAdapter
    private lateinit var nameTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homeactivity)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set click listener for friend request button
        val friendRequestButton = toolbar.findViewById<ImageButton>(R.id.friendRequestButton)
        friendRequestButton.setOnClickListener {
            // Start FriendRequestActivity
            val intent = Intent(this, FriendRequestActivity::class.java)
            startActivity(intent)
        }



        firebaseAuth = FirebaseAuth.getInstance()

        userList = ArrayList()
        userAdapter = UserAdapter(this, userList, this)

        val layoutManager = LinearLayoutManager(this)

        // Get a reference to the "users" node in the Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("users")

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val pagerAdapter = HomePagerAdapter(this)
        pagerAdapter.addFragment(ProfileFragment())
        pagerAdapter.addFragment(FriendsChatFragment())
        pagerAdapter.addFragment(SearchUsersFragment())

        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    // Use custom layout for profile tab
                    val customView = LayoutInflater.from(tab.parent!!.context)
                        .inflate(R.layout.custom_tab_profile, tab.view, false)
                    tab.customView = customView
                    val name=findViewById<ImageView>(R.id.imageView3)

                }
                1 -> tab.text = "Friends Chats"
                2 -> tab.text = "Search"
                // Add more tabs if needed
            }
        }.attach()

        // Read the user's data from the database
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        userList.add(it)
                    }
                }
                // Notify the userAdapter when data changes
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@homeactivity, "Failed to read user data.", Toast.LENGTH_SHORT).show()
            }
        })

        // Set up RecyclerView
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                // Perform logout logic here
                firebaseAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onAddFriendClicked(position: Int) {
        val selectedUser = userList[position]
        val currentUser = firebaseAuth.currentUser

        // Check if the selected user is already a friend
        if (currentUser?.uid?.let { selectedUser.friends?.contains(it) } == true) {
            Toast.makeText(this, "User is already a friend.", Toast.LENGTH_SHORT).show()
        } else {
            // Add the selected user to the current user's friend list
            currentUser?.uid?.let { currentUserId ->
                selectedUser.friends = selectedUser.friends?.plus(currentUserId to true)
            }


            // Update the current user's friend list in the database
            val currentUserRef = database.child(currentUser?.uid!!)
            currentUserRef.child("friends").setValue(selectedUser.friends?.toList())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Friend request sent.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send friend request.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }}

