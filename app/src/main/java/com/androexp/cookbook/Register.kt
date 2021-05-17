package com.androexp.cookbook

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.parse.GetCallback
import com.parse.ParseException
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.SignUpCallback
import kotlinx.android.synthetic.main.activity_register.*

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sign_up_btn.setOnClickListener { view ->
            registerUser()
        }
    }

    private fun registerUser() {
        val name = input_et_name.text.toString().trim()
        val pass = input_et_password.text.toString().trim()
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass) && name.length <= 16
            && pass.length >= 8 && pass.length <= 16) {
            checkNetwork(name, pass)
        }else if(TextUtils.isEmpty(name) || pass.length > 16){
            input_et_name.setError("Invalid user name!")
        }else if(TextUtils.isEmpty(pass) || pass.length < 8 || pass.length > 16 ){
            input_et_password.error = "Invalid password"
        }else {
            input_et_name.error = "Invalid user nsme"
            input_et_password.error = "Invalid password"
        }
    }

    private fun signIn(name: String, pass: String) {
        if(loader.visibility == View.GONE){
            loader.visibility = View.VISIBLE
        }
        val user = ParseUser.getQuery()
        user.whereEqualTo("username", name)
        user.getFirstInBackground { `object`, e ->
            if(e == null) {
                if(`object` != null) {
                    ParseUser.logInInBackground(name, pass) {user: ParseUser?, e: ParseException? ->
                        if(e == null){
                            val nameUser = ParseUser.getQuery()
                            nameUser.whereEqualTo("username", name)
                            nameUser.whereEqualTo("password", pass)
                            nameUser.getFirstInBackground{`object`, e ->
                                if(e == null){
                                    if(loader.visibility == View.VISIBLE){
                                        loader.visibility = View.GONE
                                    }
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }else{
                                    ParseUser.logOut()
                                    if(loader.visibility == View.VISIBLE){
                                        loader.visibility = View.GONE
                                    }
                                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                                }
                            }

                        }else{
                            ParseUser.logOut()
                            if(loader.visibility == View.VISIBLE){
                                loader.visibility = View.GONE
                            }
                            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }else {
                val newUser = ParseUser()
                newUser.setUsername(name)
                newUser.setPassword(pass)
                newUser.signUpInBackground { e: ParseException? ->
                    if (e == null) {
                        if (loader.visibility == View.VISIBLE) {
                            loader.visibility = View.GONE
                        }
                        Toast.makeText(
                            applicationContext,
                            "Register successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        ParseUser.logOut()
                        if (loader.visibility == View.VISIBLE) {
                            loader.visibility = View.GONE
                        }
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkNetwork(name : String, pass : String) {
        val cm : ConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info : Network? = cm.activeNetwork
        if(info != null) signIn(name, pass)
        else Snackbar.make(loader, "Network error !", Snackbar.LENGTH_SHORT).show()
        }
}