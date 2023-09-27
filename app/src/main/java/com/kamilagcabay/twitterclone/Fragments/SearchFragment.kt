package com.kamilagcabay.twitterclone.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kamilagcabay.twitterclone.Listeners.TweetListener
import com.kamilagcabay.twitterclone.Listeners.TwitterListenerImpl
import com.kamilagcabay.twitterclone.R
import com.kamilagcabay.twitterclone.Util.DATA_TWEETS
import com.kamilagcabay.twitterclone.Util.DATA_TWEET_HASHTAGS
import com.kamilagcabay.twitterclone.Util.DATA_USERS
import com.kamilagcabay.twitterclone.Util.DATA_USER_HASHTAGS
import com.kamilagcabay.twitterclone.Util.Tweet
import com.kamilagcabay.twitterclone.Util.User
import com.kamilagcabay.twitterclone.adapters.TweetListAdapter
import com.kamilagcabay.twitterclone.databinding.FragmentSearchBinding


class SearchFragment : TwitterFragment() {

    //Binding Init
    private var _binding : FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var currentHashTag = ""


    private var hashTagFollowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        listener =  TwitterListenerImpl(binding.tweetList,currentUser, callback)

        tweetsAdapter = TweetListAdapter(userId!!, arrayListOf())
        tweetsAdapter?.setListener(listener)
        binding.tweetList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tweetsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            updateList()
        }

        binding.followHashTag.setOnClickListener {
            binding.followHashTag.isClickable = false
            val followed = currentUser?.followHashTags
            if (hashTagFollowed) {
                followed?.remove(currentHashTag)
            } else {
                followed?.add(currentHashTag)
            }
            firebaseDB.collection(DATA_USERS).document(userId).update(DATA_USER_HASHTAGS,followed)
                .addOnSuccessListener {
                    callback?.onUserUpdated()
                    binding.followHashTag.isClickable = true

                }.addOnFailureListener {
                    it.printStackTrace()
                    binding.followHashTag.isClickable = true
                }
        }
    }

    fun newHashTag(term :String) {
        currentHashTag = term
        binding.followHashTag.visibility = View.VISIBLE

        updateList()

    }
    override fun updateList() {
        binding.tweetList.visibility = View.GONE

        firebaseDB.collection(DATA_TWEETS).whereArrayContains(DATA_TWEET_HASHTAGS,currentHashTag).get()
            .addOnSuccessListener {list ->
                binding.tweetList?.visibility = View.VISIBLE
                val tweets = arrayListOf<Tweet>()
                for (document in list.documents) {
                    val tweet = document.toObject(Tweet::class.java)
                    tweet?.let {
                        tweets.add(it)
                    }
                    val sortedTweets= tweets.sortedWith(compareByDescending { it.timestamp})
                    tweetsAdapter?.updateTweets(sortedTweets)
                }

            }
            .addOnFailureListener {
                it.printStackTrace()
            }
        updateFollowDrawable()
    }

    private fun updateFollowDrawable() {
        hashTagFollowed = currentUser?.followHashTags?.contains(currentHashTag) == true
        context?.let {
            if (hashTagFollowed) {
                binding.followHashTag.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.follow))
            } else {
                binding.followHashTag.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.follow_inactive))
            }
        }

    }

}