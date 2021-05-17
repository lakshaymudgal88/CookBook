package com.androexp.cookbook

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.parse.*
import kotlinx.android.synthetic.main.activity_posts_all.*
import java.io.ByteArrayOutputStream

class PostsAll : AppCompatActivity(), RecipeAdapter.RecipeClick {

    lateinit var recipeClass: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts_all)

        val string = intent.getStringExtra("recipes")
        if (string != null) {
            recipeClass = string
        }
    }

    override fun onStart() {
        super.onStart()
        checkNetwork()
    }

    private fun checkNetwork() {
        val cm: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info: Network? = cm.activeNetwork
        if (info != null) {
            getAllList(recipeClass)
        } else {
            Snackbar.make(recycle_view, "Network error !", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onRecipeClick(img: Bitmap, des: String) {
        val outputStream = ByteArrayOutputStream()
        img.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        val intent = Intent(this, RecipeDes::class.java)
        intent.putExtra("img", byteArray)
        intent.putExtra("des", des)
        startActivity(intent)
    }

    private fun getAllList(className: String) {
        recycle_view.setHasFixedSize(true)
        recycle_view.layoutManager = LinearLayoutManager(applicationContext)

        val favList = ArrayList<MainActivity.RecipeModel>()
        val user = ParseUser.getCurrentUser();
        if (user != null) {
            if (className.equals("Fav")) {
                val query = ParseQuery.getQuery<ParseObject>("Fav")
                query.whereEqualTo("user_name", ParseUser.getCurrentUser().username)
                query.findInBackground { objects, e ->
                    if (e == null) {
                        for (i in objects.indices) {
                            val recId = objects[i].get("rec_id")
                            val q = ParseQuery.getQuery<ParseObject>("Recipes")
                            q.whereEqualTo("rec_id", recId)
                            q.findInBackground { objects, e ->
                                if (e == null) {
                                    for (i in objects.indices) {
                                        val name = objects[i].get("user_name").toString()
                                        val recName = objects[i].get("rec_name").toString()
                                        val recDes = objects[i].get("rec_des").toString()
                                        val recIDs = objects[i].get("rec_id").toString()
                                        val img = objects[i].getParseFile("rec_img")
                                        img?.getDataInBackground { data, e ->
                                            if (e == null) {
                                                val bitmap = BitmapFactory.decodeByteArray(
                                                    data, 0, data.size
                                                )
                                                favList.add(
                                                    MainActivity.RecipeModel(
                                                        name, bitmap, recName, recDes, recIDs
                                                    )
                                                )
                                                val adapter =
                                                    RecipeAdapter(applicationContext, favList, this)
                                                recycle_view.adapter = adapter
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else if(className.equals("Recipes")){
                getList()
            }
        }else {
            Toast.makeText(applicationContext, "unable to find user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getList() {
        recycle_view.setHasFixedSize(true)
        recycle_view.layoutManager = LinearLayoutManager(applicationContext)

        val favList = ArrayList<MainActivity.RecipeModel>()
        val user = ParseUser.getCurrentUser();
        if (user != null) {
            val query = ParseQuery.getQuery<ParseObject>("Recipes")
            query.whereEqualTo("user_name", user.username)
            query.findInBackground { objects, e ->
                if (e == null) {
                    for (i in objects.indices) {
                        val name = objects[i].get("user_name").toString()
                        val recName = objects[i].get("rec_name").toString()
                        val recDes = objects[i].get("rec_des").toString()
                        val recId = objects[i].get("rec_id").toString()
                        val img = objects[i].getParseFile("rec_img")
                        img?.getDataInBackground { data, e ->
                            if (e == null) {
                                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                                favList.add(
                                    MainActivity.RecipeModel(
                                        name,
                                        bitmap,
                                        recName,
                                        recDes,
                                        recId
                                    )
                                )
                                val adapter = RecipeAdapter(applicationContext, favList, this)
                                recycle_view.adapter = adapter
                            }
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(applicationContext, "user null", Toast.LENGTH_SHORT).show()
        }
    }

}