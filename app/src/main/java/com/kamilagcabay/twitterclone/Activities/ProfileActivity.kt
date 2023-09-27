package com.kamilagcabay.twitterclone.Activities


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kamilagcabay.twitterclone.R
import com.kamilagcabay.twitterclone.Util.DATA_IMAGES
import com.kamilagcabay.twitterclone.Util.DATA_USERS
import com.kamilagcabay.twitterclone.Util.DATA_USER_EMAİL
import com.kamilagcabay.twitterclone.Util.DATA_USER_IMAGE_URL
import com.kamilagcabay.twitterclone.Util.DATA_USER_USERNAME
import com.kamilagcabay.twitterclone.Util.REQUEST_CODE_PHOTO
import com.kamilagcabay.twitterclone.Util.User
import com.kamilagcabay.twitterclone.Util.loadUrl
import com.kamilagcabay.twitterclone.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    //View Binding
    private lateinit var binding: ActivityProfileBinding

    //Firebase Database
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId= FirebaseAuth.getInstance().currentUser?.uid

    //Other Properties
    private var imageUrl :String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        if (userId == null) {
            finish()
        }
        binding.profileProgressLayout.setOnTouchListener { v, event ->
            true
        }

        populateInfo()

        binding.photoIV.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }



        binding.back.setOnClickListener {
            val intent = Intent(this,HomeActivity::class.java)
            startActivity(intent)

        }
    }

    fun populateInfo() {
        binding.profileProgressLayout.visibility = View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                binding.usernameET.setText(user?.username , TextView.BufferType.EDITABLE)
                binding.emailET.setText(user?.email, TextView.BufferType.EDITABLE)
                imageUrl = user?.imageUrl
                imageUrl?.let {
                    binding.photoIV.loadUrl(user?.imageUrl, R.drawable.logo)
                }
                binding.profileProgressLayout.visibility = View.GONE

            }.addOnFailureListener {
                it.printStackTrace()
                finish()
            }
    }


    fun onApply(v: View) {

        binding.profileProgressLayout.visibility = View.VISIBLE
        val username = binding.usernameET.text.toString()
        val email = binding.emailET.text.toString()
        val map = HashMap<String, Any>()
        map[DATA_USER_USERNAME] = username
        map[DATA_USER_EMAİL] = email

    firebaseDB.collection(DATA_USERS).document(userId!!).update(map)
        .addOnSuccessListener {
            Toast.makeText(this, "Update successful", Toast.LENGTH_LONG).show()
            finish()
        }
        .addOnFailureListener {
            it.printStackTrace()
            Toast.makeText(this, "Update Failed!! Please try again", Toast.LENGTH_LONG).show()
            binding.profileProgressLayout.visibility = View.GONE

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)
        }
    }

    fun storeImage(imageUri: Uri?) {
        imageUri?.let {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            binding.profileProgressLayout.visibility = View.VISIBLE

            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {uri ->
                            val url = uri.toString()
                            firebaseDB.collection(DATA_USERS).document(userId!!).update(DATA_USER_IMAGE_URL, url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    binding.photoIV.loadUrl(imageUrl, R.drawable.logo)
                                }
                            binding.profileProgressLayout.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            onUploadFailure()
                        }
                }
                .addOnFailureListener {
                    onUploadFailure()
                }
        }
    }

    fun onUploadFailure() {
        Toast.makeText(this, "Image upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
        binding.profileProgressLayout.visibility = View.GONE
    }

    fun onSignOut(v: View) {
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
        finish()

    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}