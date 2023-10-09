

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.test1.R
import com.example.test1.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class ProfileFragment : Fragment() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imageView: ImageView
    private lateinit var profileName: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: FirebaseStorage
    private lateinit var selectedImg: Uri
    private lateinit var dialog: AlertDialog.Builder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        dialog = AlertDialog.Builder(context)
            .setMessage("Updating Profile...")
            .setCancelable(false)
        database = FirebaseDatabase.getInstance()
        storageReference = FirebaseStorage.getInstance()
        firebaseAuth=FirebaseAuth.getInstance()
        val imageButton: ImageButton = view.findViewById(R.id.imageButton)
        imageView = view.findViewById(R.id.imageView2)
        profileName = view.findViewById(R.id.profile_name)

        // Retrieve user name from Firebase Realtime Database
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("user").child(uid!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Handle the retrieved data here
                val user = dataSnapshot.getValue(User::class.java)
                profileName.text = user?.name

                // Load the profile picture into the ImageView using Picasso
                user?.profilePictureUrl?.let { url ->
                    Picasso.get().load(url).into(imageView)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error case here
            }
        })

        imageButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }


        return view
    }
    private fun uploadData(){
        val reference = storageReference.reference.child("profile").child(Date().time.toString())
        reference.putFile(selectedImg).addOnCompleteListener{
            if (it.isSuccessful){
                reference.downloadUrl.addOnSuccessListener { task ->
                    uploadInfo(task.toString())
                }
            }
        }
    }

    private fun uploadInfo(imgurl: String) {
        database.reference.child("user").child(firebaseAuth.uid!!).child("profilePictureUrl").setValue(imgurl)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null){
            if(data.data!=null){
                selectedImg=data.data!!
                imageView.setImageURI(selectedImg)
                uploadData()
            }
        }

    }
}