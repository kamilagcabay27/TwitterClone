package com.kamilagcabay.twitterclone.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.kamilagcabay.twitterclone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val firebaseAuth  = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomeActivity.newIntent(this))
            finish()
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTextChangedListener(binding.emailET,binding.emailTIL)
        setTextChangedListener(binding.passwordET,binding.passwordTIL)
        binding.loginProgressLayout.setOnTouchListener { v, event -> true }

    }
    fun setTextChangedListener(et :EditText, til :TextInputLayout) {
        et.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    fun onLogin(view: View) {
        var proceed = true
        if (binding.emailET.text.isNullOrEmpty()) {
            binding.emailTIL.error = "Email is required"
            binding.emailTIL.isErrorEnabled = true
            proceed = false
        }
        if (binding.passwordET.text.isNullOrEmpty()) {
            binding.passwordTIL.error = "Password is required"
            binding.passwordTIL.isErrorEnabled = true
            proceed = false
        }
        if (proceed) {
            binding.loginProgressLayout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(binding.emailET.text.toString(), binding.passwordET.text.toString())
                .addOnCompleteListener {
                    if (!it.isSuccessful){
                        binding.loginProgressLayout.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Login Error: ${it.exception?.localizedMessage}", Toast.LENGTH_LONG).show()

                    }
                }.addOnFailureListener {
                    it.printStackTrace()
                    binding.loginProgressLayout.visibility = View.GONE
                }
        }
    }

    fun gotToSignUp (view: View) {

        startActivity(SignupActivity.newIntent(this))
        finish()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)

    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }
    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}