package com.kamilagcabay.twitterclone.Activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.kamilagcabay.twitterclone.Fragments.HomeFragment
import com.kamilagcabay.twitterclone.Fragments.MyActivityFragment
import com.kamilagcabay.twitterclone.Fragments.SearchFragment
import com.kamilagcabay.twitterclone.Fragments.TwitterFragment
import com.kamilagcabay.twitterclone.Listeners.HomeCallback
import com.kamilagcabay.twitterclone.R
import com.kamilagcabay.twitterclone.Util.DATA_USERS
import com.kamilagcabay.twitterclone.Util.User
import com.kamilagcabay.twitterclone.Util.loadUrl
import com.kamilagcabay.twitterclone.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), HomeCallback {
    private var sectionPagerAdapter: SectionPagerAdapter? = null

    private lateinit var binding: ActivityHomeBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val homeFragment  = HomeFragment()
    private val searchFragment = SearchFragment()
    private val myActivityFragment = MyActivityFragment()
    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private var user : User? = null

    private var currentFragment :TwitterFragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        sectionPagerAdapter = SectionPagerAdapter(supportFragmentManager)


        binding.container.adapter = sectionPagerAdapter
        binding.container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabs))
        binding.tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(binding.container))
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                when(tab?.position) {
                    0 -> {
                        binding.titleBar.visibility = View.VISIBLE
                        binding.titleBar.text = "Home"
                        binding.searchBar.visibility = View.GONE
                        currentFragment = homeFragment
                    }
                    1 -> {
                        binding.titleBar.visibility = View.GONE
                        binding.searchBar.visibility = View.VISIBLE
                        currentFragment = searchFragment
                    }
                    2 -> {
                        binding.titleBar.visibility = View.VISIBLE
                        binding.titleBar.text = "My Activity"
                        binding.searchBar.visibility = View.GONE
                        currentFragment = myActivityFragment
                    }
                }
            }

            override fun onTabUnselected(tab: Tab?) {

            }

            override fun onTabReselected(tab: Tab?) {

            }

        })

        binding.logo.setOnClickListener {
            startActivity(ProfileActivity.newIntent(this))
        }

        //Entering tweet Activity
        binding.fab.setOnClickListener {
            startActivity(TweetActivity.newIntent(this, userId, user?.username))
        }
        binding.homeProgressLayout.setOnTouchListener { v, event -> true }

        binding.search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFragment.newHashTag(v?.text.toString())
            }
            true
        }

    }
    /*
    fun onLogout(v: View) {
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

     */



    override fun onResume() {
        super.onResume()
        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId==null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        } else {
            populate()
        }
    }
    override fun onUserUpdated() {
        populate()
    }
    override fun onRefresh() {
        currentFragment.updateList()
    }

    fun populate() {
        binding.homeProgressLayout.visibility = View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener {documentSnapshot ->
                binding.homeProgressLayout.visibility = View.GONE
                user = documentSnapshot.toObject(User::class.java)
                user?.imageUrl?.let {
                    binding.logo.loadUrl(it,R.drawable.logo)

                }
                updateFragmentUser()
            }.addOnFailureListener {
                it.printStackTrace()
                finish()
            }
    }

    fun updateFragmentUser() {
        homeFragment.setUser(user)
        searchFragment.setUser(user)
        myActivityFragment.setUser(user)
        currentFragment.updateList()
    }

    inner class SectionPagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount() = 3

        override fun getItem(position: Int): Fragment {
            return when(position){
                0 -> homeFragment
                1-> searchFragment
                else -> myActivityFragment
            }
        }

    }

    companion object {
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }


}