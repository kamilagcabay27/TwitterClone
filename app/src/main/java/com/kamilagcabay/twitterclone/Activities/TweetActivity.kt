package com.kamilagcabay.twitterclone.Activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kamilagcabay.twitterclone.R
import com.kamilagcabay.twitterclone.Util.DATA_IMAGES
import com.kamilagcabay.twitterclone.Util.DATA_TWEETS
import com.kamilagcabay.twitterclone.Util.REQUEST_CODE_PHOTO
import com.kamilagcabay.twitterclone.Util.Tweet
import com.kamilagcabay.twitterclone.Util.loadUrl
import com.kamilagcabay.twitterclone.databinding.ActivityTweetBinding

class TweetActivity : AppCompatActivity() {
    private lateinit var binding : ActivityTweetBinding


    //Firebase Database
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference

    // Other properties
    private var imageUrl :String? = null
    private var userId :  String?  = null
    private var userName : String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTweetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        if (intent.hasExtra(PARAM_USER_ID) && intent.hasExtra(PARAM_USER_NAME)) {
            userId = intent.getStringExtra(PARAM_USER_ID)
            userName = intent.getStringExtra(PARAM_USER_NAME)

        } else {
            Toast.makeText(this, "Error Creating Tweet" , Toast.LENGTH_LONG).show()
            finish()
        }


        binding.back2.setOnClickListener {
            val intent = Intent(this@TweetActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.tweetProgressLayout.setOnTouchListener { v, event -> true }
    }


    fun addImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PHOTO)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)
        }
    }

    fun storeImage(imageUri : Uri?) {
        imageUri?.let {
            Toast.makeText(this, "Uploading..." , Toast.LENGTH_LONG).show()
            binding.tweetProgressLayout.visibility = View.VISIBLE

            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filePath.putFile(imageUri)
                .addOnSuccessListener {taskSnapshot ->
                    filePath.downloadUrl
                        .addOnSuccessListener {
                            imageUrl = it.toString()
                            binding.tweetImage.loadUrl(imageUrl, R.drawable.logo)

                            binding.tweetProgressLayout.visibility = View.GONE



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
        Toast.makeText(this, "Image Upload Failed. Please try again later.", Toast.LENGTH_LONG).show()
        binding.tweetProgressLayout.visibility = View.GONE
    }

    fun postTweet(view: View) {
        binding.tweetProgressLayout.visibility = View.VISIBLE
        val text = binding.tweetText.text.toString()
        val hashtags = getHashtags(text)
        val tweetId = firebaseDB.collection(DATA_TWEETS).document()
        val tweet = Tweet(tweetId.id, arrayListOf(userId!!), userName,  text,imageUrl, System.currentTimeMillis(), hashtags, arrayListOf())
        tweetId.set(tweet)
            .addOnCompleteListener {
                finish()
            }.addOnFailureListener { e ->
                e.printStackTrace()
                binding.tweetProgressLayout.visibility = View.GONE
                Toast.makeText(this, "Failed to post the tweet", Toast.LENGTH_LONG).show()
            }
    }


    private fun getHashtags(source : String) :ArrayList<String> {
        val hashtags = arrayListOf<String>()
        var text = source

        while (text.contains("#")) {
            var hastag = ""
            val hash = text.indexOf("#")
            text = text.substring(hash + 1)

            val firstSpace = text.indexOf(" ")
            val firstHash= text.indexOf("#")

            if (firstSpace == -1 && firstHash == -1) {
                hastag = text.substring(0)
            } else if (firstSpace != -1 && firstSpace < firstHash) {
                hastag = text.substring(0, firstSpace)
                text = text.substring(firstSpace + 1)
            } else {
                hastag = text.substring(0, firstHash)
                text = text.substring(firstHash)
            }

            if (!hastag.isNullOrEmpty()) {
                hashtags.add(hastag)
            }
        }

        return hashtags
    }

    companion object {
        val PARAM_USER_ID = "UserId"
        val PARAM_USER_NAME = "UserName"


        fun newIntent(context: Context, userId : String?, userName:String?) : Intent {
            val intent = Intent(context, TweetActivity::class.java)
            intent.putExtra(PARAM_USER_NAME, userName)
            intent.putExtra(PARAM_USER_ID, userId)

            return intent
        }
    }
}

