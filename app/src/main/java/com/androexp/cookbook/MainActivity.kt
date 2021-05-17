package com.androexp.cookbook

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.parse.GetDataCallback
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(), RecipeAdapter.RecipeClick {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
    }

    private fun initUI() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val toolbar: MaterialToolbar = findViewById(R.id.tab_layout)
        val postBtn: FloatingActionButton = findViewById(R.id.fab_add_post)

        setTabFun(toolbar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        postBtn.setOnClickListener { view ->
            if (ParseUser.getCurrentUser() == null) {
                AlertDialog.Builder(this)
                    .setTitle("Post Recipe")
                    .setMessage("You need to Sign In before posting any recipe")
                    .setPositiveButton("Sign In") { _, i ->
                        startActivity(Intent(this, Register::class.java))
                    }
                    .setNegativeButton("It's OK") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }.create().show()
            } else {
                startActivity(Intent(this, PostRecipe::class.java))
            }
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
        if (info != null) recipeList()
        else Snackbar.make(fab_add_post, "Network error !", Snackbar.LENGTH_SHORT).show()
    }

    private fun setTabFun(toolbar: MaterialToolbar) {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.tab_search -> {
                    Toast.makeText(applicationContext, "Search", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.tab_more_menu -> {
                    setUpMenu()
                    true
                }
                else -> false
            }
        }
    }

    private fun setUpMenu() {
        val v: View = findViewById(R.id.tab_more_menu)
        val menu = PopupMenu(this, v)
        menu.menuInflater.inflate(R.menu.more_menu_item, menu.menu)
        menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_fav -> {
                    val intent = Intent(this, PostsAll::class.java)
                    intent.putExtra("recipes", "Fav")
                    startActivity(intent)
                }
                R.id.more_posts -> {
                    val intent = Intent(this, PostsAll::class.java)
                    intent.putExtra("recipes", "Recipes")
                    startActivity(intent)
                }
                R.id.more_sign_out -> {
                    val user = ParseUser.getCurrentUser()
                    if (user != null) {
                        ParseUser.logOut()
                    }
                }
            }
            true
        })
        menu.show()
    }

    private fun recipeList() {
        val recipeList = ArrayList<RecipeModel>()
        val query = ParseQuery<ParseObject>("Recipes")
        query.findInBackground { objects, e ->
            if (e == null) {
                if (objects != null) {
                    for (i in objects.indices) {
                        val userName = objects[i].get("user_name").toString()
                        val recName = objects[i].get("rec_name").toString()
                        val recDes = objects[i].get("rec_des").toString()
                        val recId = objects[i].get("rec_id").toString()
                        val recImg = objects[i].getParseFile("rec_img")
                        recImg?.getDataInBackground(GetDataCallback { data, e ->
                            if (e == null) {
                                if (data != null) {
                                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                                    recipeList.add(RecipeModel(userName, bitmap, recName, recDes, recId))
                                    val adapter = RecipeAdapter(applicationContext, recipeList, this
                                    )
                                    recycler_view.adapter = adapter
                                }
                            } else {
                                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                    }
                }
            } else {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    if(fab_add_post.visibility == View.GONE){
                        fab_add_post.visibility = View.VISIBLE
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy != 0){
                    if(fab_add_post.visibility == View.VISIBLE){
                        fab_add_post.visibility = View.GONE
                    }
                }
            }
        })
    }

    data class RecipeModel(
        val userName: String,
        val img: Bitmap,
        val recName: String,
        val recDes: String,
        val recId: String
    )

    override fun onRecipeClick(img: Bitmap, des: String) {
        val outputStream = ByteArrayOutputStream()
        img.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        val intent = Intent(this, RecipeDes::class.java)
        intent.putExtra("img", byteArray)
        intent.putExtra("des", des)
        startActivity(intent)
    }

}