package com.kamilagcabay.twitterclone.Util


data class User(
    val email : String? = "",
    val username :String? ="",
    val imageUrl : String ? = "",
    val followHashTags : ArrayList<String>? = arrayListOf(),
    val followUsers : ArrayList<String> ? =  arrayListOf()
)

data class Tweet(
    val tweetId : String? = "",
    val userIds : ArrayList<String>? = arrayListOf(),
    val username: String ? = "",
    val text :String? ="",
    val imageUrl: String? ="",
    val timestamp: Long? = 0,
    val hashtags : ArrayList<String>? = arrayListOf(),
    val likes : ArrayList<String>? = arrayListOf()
)