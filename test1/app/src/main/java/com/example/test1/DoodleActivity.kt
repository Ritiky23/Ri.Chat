package com.example.test1

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.TRANSPARENT
import android.graphics.PixelFormat
import android.graphics.PixelFormat.TRANSPARENT
import android.icu.lang.UCharacter.JoiningType.TRANSPARENT
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.test1.R
import java.io.ByteArrayOutputStream


class DoodleActivity : AppCompatActivity() {

    private lateinit var doodleView: DoodleView
    private lateinit var frameLayout: FrameLayout
    private lateinit var doodbtn:Button
    private lateinit var receiverId: String
    private lateinit var senderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doodle)
        doodbtn=findViewById(R.id.doodlesnd)
        // Set the activity to fullscreen
        window.setBackgroundDrawableResource(android.R.color.transparent)
        receiverId = intent.getStringExtra("receiverId") ?: ""
        senderId = intent.getStringExtra("senderId") ?: ""
        // Initialize the DoodleView
        doodleView = DoodleView(this, null)
        doodleView.setBackgroundResource(R.drawable.roundcorner1)


// Replace with your actual background resource

// Set the background color for the DoodleView


        // Check if the app has the SYSTEM_ALERT_WINDOW permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Request the permission
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 0)
        } else {
            // Permission already granted or running on a device below Android 6.0
            // Add the code to initialize and display the floating window here
            showFloatingWindow()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            // Permission granted, add the code to initialize and display the floating window here
            showFloatingWindow()
        } else {
            // Permission not granted, display a message or take appropriate action
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingWindow()
    }

    private fun removeFloatingWindow() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val frameLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )
        windowManager.removeViewImmediate(doodleView)
        windowManager.removeViewImmediate(frameLayout)
    }

    private fun showFloatingWindow() {
        // Set up the layout parameters for the floating window
        val params = WindowManager.LayoutParams(
            500,
            500,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )

        // Set the gravity and positioning of the floating window
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        params.x = 0 // Adjust the X position if needed
        params.y = resources.getDimensionPixelSize(R.dimen.erase_button_margin_bottom) // Adjust the Y position if needed

        // Apply the layout parameters to the floating window
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(doodleView, params)

        // Create a FrameLayout to hold the erase button
        frameLayout = FrameLayout(this)
        frameLayout.setBackgroundColor(Color.TRANSPARENT)
        val frameLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )
        frameLayoutParams.gravity = Gravity.BOTTOM or Gravity.END
        frameLayoutParams.x = resources.getDimensionPixelSize(R.dimen.erase_button_margin_right)
        frameLayoutParams.y = resources.getDimensionPixelSize(R.dimen.erase_button_margin_bottom)
        windowManager.addView(frameLayout, frameLayoutParams)

        // Add the erase button to the FrameLayout
        val clearButton = Button(this)
        val icon = ContextCompat.getDrawable(this, R.drawable.reset1)
        val buttonSize = resources.getDimensionPixelSize(R.dimen.erase_button_size) // Replace R.dimen.clear_button_size with the dimension resource ID for the button size
        val layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
        clearButton.layoutParams = layoutParams

        // Set the background resource for the clear button
        clearButton.setBackgroundResource(R.drawable.roundcorner1)

        // Set the icon for the clear button
        clearButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.reset1, 0, 0, 0)

        // Set the icon for the clear button

        clearButton.setOnClickListener {
            doodleView.clearDrawing() // Clear the drawing on button click
        }
        frameLayout.addView(clearButton)

        doodbtn.setOnClickListener {
            // Get the drawing data from the DoodleView
            val drawingData = doodleView.getDrawingData()

            // Convert the drawing data to a byte array
            val bitmap = Bitmap.createBitmap(doodleView.width, doodleView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            doodleView.draw(canvas)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            // Create an intent to pass the drawing data back to the ChatActivity
            val intent = Intent()
            intent.putExtra("drawingData", byteArray)
            intent.putExtra("receiverId", receiverId)
            intent.putExtra("senderId", senderId)

            // Set the result to indicate a successful operation and pass the intent
            setResult(RESULT_OK, intent)

            // Finish the DoodleActivity and return to the ChatActivity
            finish()
        }
    }}
