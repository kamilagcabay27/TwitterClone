package com.kamilagcabay.twitterclone.Listeners

import com.kamilagcabay.twitterclone.Util.Tweet

interface TweetListener {

    fun onLayoutClick(tweet: Tweet?)
    fun onLike(tweet: Tweet?)
    fun onRetweet(tweet: Tweet?)
}