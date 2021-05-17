package com.androexp.cookbook

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.snackbar.Snackbar
import com.parse.*
import kotlinx.android.synthetic.main.activity_post_recipe.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PostRecipe : AppCompatActivity() {

    val GALLERY_RA: Int = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_recipe)

        handleBtnClicks()
    }

    private fun handleBtnClicks() {
        back_btn.setOnClickListener { view ->
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        post_btn.setOnClickListener { view ->
            if (image_view.drawable != null && des_et.text.toString() != "" && input_et_recipe_name.text.toString() != "" &&
                input_et_recipe_name.text.toString().length <= 30
            ) {
                val bitmap = image_view.drawable.toBitmap()
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                val byteArray = outputStream.toByteArray()
                postRecipe(des_et.text.toString(), input_et_recipe_name.text.toString(), byteArray)
            } else if (image_view.drawable == null) {
                Toast.makeText(applicationContext, "attach an image first", Toast.LENGTH_SHORT)
                    .show()
            } else if (des_et.text.toString().equals("")) {
                Toast.makeText(
                    applicationContext,
                    "please write the recipe des first",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (input_et_recipe_name.text.toString()
                    .equals("") || input_et_recipe_name.text.toString().length > 30
            ) {
                Toast.makeText(
                    applicationContext,
                    "invalid recipe name",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "attach an image & write des",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        add_img_btn.setOnClickListener { view ->
            addImage()
        }
    }

    private fun postRecipe(recDes: String, recName: String, byteArray: ByteArray) {
        if(loader.visibility == View.GONE){
            loader.visibility = View.VISIBLE
        }
        val user = ParseUser.getCurrentUser();
        if (user != null) {
            val dateFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
            val date = dateFormat.format(Date())
            val parseFile =
                ParseFile(ParseUser.getCurrentUser().username + date + ".png", byteArray)
            parseFile.saveInBackground { e: ParseException? ->
                if (e == null) {
                    Toast.makeText(applicationContext, "1", Toast.LENGTH_SHORT).show()
                    val obj = ParseObject("Recipes")
                    obj.put("user_name", user.username)
                    obj.put("rec_des", recDes)
                    obj.put("rec_name", recName)
                    obj.put("rec_img", parseFile)
                    obj.put("rec_id", user.username + System.currentTimeMillis())
                    obj.saveInBackground(SaveCallback { e: ParseException? ->
                        if(e == null){
                            Toast.makeText(applicationContext, "Recipe Posted Sucessfully", Toast.LENGTH_SHORT).show()
                            if(loader.visibility == View.VISIBLE){
                                loader.visibility = View.GONE
                            }
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    })
                } else {
                    if(loader.visibility == View.VISIBLE){
                        loader.visibility = View.GONE
                    }
                    Toast.makeText(applicationContext, "error in saving: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            if(loader.visibility == View.VISIBLE){
                loader.visibility = View.GONE
            }
            Toast.makeText(applicationContext, "unable to find user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_RA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_RA && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data!!
            image_view.setImageURI(uri)
            if (add_img_btn.visibility == View.VISIBLE) {
                add_img_btn.visibility = View.GONE
            }
        }
    }

}