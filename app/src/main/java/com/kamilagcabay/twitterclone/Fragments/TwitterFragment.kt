package com.kamilagcabay.twitterclone.Fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kamilagcabay.twitterclone.Listeners.HomeCallback
import com.kamilagcabay.twitterclone.Listeners.TweetListener
import com.kamilagcabay.twitterclone.Listeners.TwitterListenerImpl
import com.kamilagcabay.twitterclone.Util.User
import com.kamilagcabay.twitterclone.adapters.TweetListAdapter
import java.lang.RuntimeException

abstract class TwitterFragment : Fragment() {
    //Firebase Database
    protected val firebaseDB = FirebaseFirestore.getInstance()
    protected val userId = FirebaseAuth.getInstance().currentUser?.uid

    protected var listener: TwitterListenerImpl? = null

    //Adapters
    protected var tweetsAdapter: TweetListAdapter? = null

    //Other Properties
    protected var currentUser: User? = null

    protected var callback : HomeCallback? = null



    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeCallback) {
            callback = context
        } else{
            throw RuntimeException(context.toString() + "Must Implemented HomeCallback")
        }
    }



    fun setUser(user : User?) {
        this.currentUser = user
        listener?.user = user

    }

    abstract fun updateList()

    override fun onResume() {
        super.onResume()
        updateList()
    }

}