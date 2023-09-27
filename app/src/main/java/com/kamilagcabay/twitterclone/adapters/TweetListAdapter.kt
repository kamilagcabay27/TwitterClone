package com.kamilagcabay.twitterclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kamilagcabay.twitterclone.Listeners.TweetListener
import com.kamilagcabay.twitterclone.R
import com.kamilagcabay.twitterclone.Util.Tweet
import com.kamilagcabay.twitterclone.Util.getDate
import com.kamilagcabay.twitterclone.Util.loadUrl
import com.kamilagcabay.twitterclone.databinding.ItemTweetBinding

class TweetListAdapter(val userId : String, val tweets : ArrayList<Tweet>) : RecyclerView.Adapter<TweetListAdapter.TweetListHolder>() {


    private var listener :TweetListener? = null

    fun setListener(listener: TweetListener?)  {
        this.listener = listener
    }
    fun updateTweets(newTweets :List<Tweet>) {
        tweets.clear()
        tweets.addAll(newTweets)
        notifyDataSetChanged()
    }

    class TweetListHolder (val binding : ItemTweetBinding) : RecyclerView.ViewHolder(binding.root) {

        private val layout = binding.tweetLayout
        private val username = binding.tweetUsername
        private val text = binding.tweetText
        private val image = binding.tweetImage
        private val date = binding.tweetDate
        private val like = binding.tweetLike
        private val likeCount = binding.tweetLikeCount
        private val retweet = binding.tweetRetweet
        private val retweetCount = binding.tweetRetweetCount

        fun bind(userId: String, tweet: Tweet, listener : TweetListener?) {
            username.text = tweet.username
            text.text  = tweet.text
            if (tweet.imageUrl.isNullOrEmpty()) {
                image.visibility = View.GONE
            } else {
                image.visibility = View.VISIBLE
                image.loadUrl(tweet.imageUrl)
            }

            date.text = getDate(tweet.timestamp)
            likeCount.text = tweet.likes?.size.toString()
            retweetCount.text = tweet.userIds?.size?.minus(1).toString()
            layout.setOnClickListener { listener?.onLayoutClick(tweet) }
            like.setOnClickListener { listener?.onLike(tweet) }
            retweet.setOnClickListener { listener?.onRetweet(tweet) }

            if (tweet.likes?.contains(userId) == true) {
                like.setImageDrawable(ContextCompat.getDrawable(like.context, R.drawable.like))
            } else {
                like.setImageDrawable(ContextCompat.getDrawable(like.context, R.drawable.like_inactive))

            }

            if (tweet.userIds?.get(0).equals(userId)){
                retweet.setImageDrawable(ContextCompat.getDrawable(retweet.context, R.drawable.original))
                retweet.isClickable = false
            } else if (tweet.userIds?.contains(userId) == true) {
                retweet.setImageDrawable(ContextCompat.getDrawable(retweet.context, R.drawable.retweet))

            } else {
                retweet.setImageDrawable(ContextCompat.getDrawable(retweet.context, R.drawable.retweet_inactive))

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : TweetListHolder {
        val binding = ItemTweetBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return TweetListHolder(binding)
    }

    override fun getItemCount() = tweets.size

    override fun onBindViewHolder(holder: TweetListHolder, position: Int) {
        holder.bind(userId,tweets[position],listener)
    }
}


