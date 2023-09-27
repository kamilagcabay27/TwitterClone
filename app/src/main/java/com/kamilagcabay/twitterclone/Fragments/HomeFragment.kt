package com.kamilagcabay.twitterclone.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kamilagcabay.twitterclone.Listeners.TwitterListenerImpl
import com.kamilagcabay.twitterclone.R
import com.kamilagcabay.twitterclone.Util.DATA_TWEETS
import com.kamilagcabay.twitterclone.Util.DATA_TWEET_HASHTAGS
import com.kamilagcabay.twitterclone.Util.Tweet
import com.kamilagcabay.twitterclone.adapters.TweetListAdapter
import com.kamilagcabay.twitterclone.databinding.FragmentHomeBinding

class HomeFragment : TwitterFragment() {

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = TwitterListenerImpl(binding.tweetList, currentUser, callback)

        tweetsAdapter = TweetListAdapter(userId!!, arrayListOf())
        tweetsAdapter?.setListener(listener)
        binding.tweetList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tweetsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            updateList()
        }

    }


    override fun updateList() {

        binding.tweetList?.visibility = View.GONE
        currentUser?.let {
            val tweets = arrayListOf<Tweet>()

            for (hashtag in it.followHashTags!!){
                firebaseDB.collection(DATA_TWEETS).whereArrayContains(DATA_TWEET_HASHTAGS, hashtag).get()
                    .addOnSuccessListener {
                        for (document in it.documents) {
                            val tweet = document.toObject(Tweet::class.java)
                            tweet?.let {
                                tweets.add(it)
                            }
                            updateAdapter(tweets)
                            binding.tweetList?.visibility = View.VISIBLE
                        }
                    }.addOnFailureListener {
                        it.printStackTrace()
                        binding.tweetList?.visibility = View.VISIBLE
                    }
            }
        }

    }

    private fun updateAdapter(tweets : List<Tweet>) {
        val sortedTweets = tweets.sortedWith(compareByDescending { it.timestamp })
        tweetsAdapter?.updateTweets(removeDuplicates(sortedTweets))
    }

    private fun removeDuplicates(originalList : List<Tweet>) = originalList.distinctBy { it.tweetId }
}