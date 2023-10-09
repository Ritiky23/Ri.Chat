package com.example.test1

import ChatMessage
import MessageAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Base64
import android.util.DisplayMetrics
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ChatActivity : AppCompatActivity() {
    private lateinit var messageref: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageBox:EditText
    private lateinit var sendButton: Button
    private lateinit var doodlebutton:ImageButton
    private lateinit var messageList: ArrayList<ChatMessage>
    private lateinit var doodleImageView: ImageView
    private var doodleBitmap: Bitmap? = null
    private var senderUid: String? = null

    private var receiverUid: String? = null
    var receiverRoom:String?=null
    var senderRoom:String?=null
    companion object {
        private const val DOODLE_ACTIVITY_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val receiverUid = intent.getStringExtra("friendID")
        val name = intent.getStringExtra("name")


        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
        val color = Color.parseColor("#272643") // Replace "#FF0000" with your desired color code
        val drawable = ColorDrawable(color)
        actionBar?.setBackgroundDrawable(drawable)
        actionBar?.setHomeAsUpIndicator(R.drawable.back_cht1) // Set the back button icon

        val customActionBarView = layoutInflater.inflate(R.layout.actionbar_chat, null)
        val profilePictureImageView: ImageView = customActionBarView.findViewById(R.id.profile_picture)
        val name1:TextView=customActionBarView.findViewById(R.id.profile_name)


// Load the profile picture from your data source and set it to the ImageView


        actionBar?.customView = customActionBarView


        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val databaseReference = FirebaseDatabase.getInstance().getReference("user").child(receiverUid!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Handle the retrieved data here
                val user = dataSnapshot.getValue(User::class.java)
                name1.text = user?.name

                // Load the profile picture into the ImageView using Picasso
                user?.profilePictureUrl?.let { url ->
                    Picasso.get().load(url).into(profilePictureImageView)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error case here
            }
        })


        messageref = FirebaseDatabase.getInstance().getReference()
        val senderUid=FirebaseAuth.getInstance().currentUser?.uid
        receiverRoom= receiverUid + senderUid
        senderRoom= senderUid + receiverUid


        messageRecyclerView=findViewById(R.id.chatRecyclerView)
        messageBox=findViewById(R.id.messagetedit)
        sendButton=findViewById(R.id.sendbtn)
        messageList= ArrayList()
        messageAdapter=MessageAdapter(this,messageList)
        doodlebutton=findViewById(R.id.cameraButton)
        doodlebutton.setOnClickListener {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            val intent = Intent(this, DoodleActivity::class.java)
            intent.putExtra("screenWidth", screenWidth)
            intent.putExtra("screenHeight", screenHeight)
            intent.putExtra("receiverId", receiverUid)
            intent.putExtra("senderId", senderUid)
            startActivityForResult(intent, DOODLE_ACTIVITY_REQUEST_CODE)
        }


        messageRecyclerView.layoutManager=LinearLayoutManager(this)
        messageRecyclerView.adapter = messageAdapter
        //logic for adding data to recyclerview

        messageref.child("chats").child(senderRoom!!).child("messages").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.getValue(ChatMessage::class.java)
                    messageList.add(message!!)
                }
                messageAdapter.notifyDataSetChanged()
                scrollToLastMessage()


                // Check if the last message contains an encoded image

            }

            // Rest of the code...



            override fun onCancelled(error: DatabaseError) {
            }

        })


        sendButton.setOnClickListener {
            val message1 = messageBox.text.toString()
            if (message1.isNotEmpty()) {

                val messageObj=ChatMessage(message1, senderUid,receiverUid, null, null)
                messageref.child("chats").child(senderRoom!!).child("messages").push().setValue(messageObj).addOnSuccessListener {
                    messageref.child("chats").child(receiverRoom!!).child("messages").push().setValue(messageObj)}}
            messageBox.setText("")
            val itemCount = messageAdapter.itemCount
            if (itemCount > 0) {
                messageRecyclerView.scrollToPosition(itemCount - 1)
            }
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DOODLE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieve the drawing data from the intent
            val drawingData = data?.getByteArrayExtra("drawingData")
            val receiverId = data?.getStringExtra("receiverId")
            val senderUid = data?.getStringExtra("senderId")
            // Use the drawing data as needed (e.g., send it to the recipient)
            if (drawingData != null) {
                val encodedImage = Base64.encodeToString(drawingData, Base64.DEFAULT)
                val message = ChatMessage("", senderUid,receiverId,null,encodedImage)
                val senderMessageRef = messageref.child("chats").child(senderRoom!!).child("messages").push()
                val receiverMessageRef = messageref.child("chats").child(receiverRoom!!).child("messages").push()

                senderMessageRef.setValue(message).addOnSuccessListener {
                    receiverMessageRef.setValue(message).addOnSuccessListener {
                        // Doodle image sent successfully to the receiver
                    }.addOnFailureListener { exception ->
                        // Handle failure when sending doodle image to the receiver
                    }
                }.addOnFailureListener { exception ->
                    // Handle failure when sending doodle image
                }
            }
        }
    }



    private fun scrollToLastMessage() {
        val itemCount = messageAdapter.itemCount
        if (itemCount > 0) {
            messageRecyclerView.scrollToPosition(itemCount - 1)
        }
    }
}
