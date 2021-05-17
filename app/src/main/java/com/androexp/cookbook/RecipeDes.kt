package com.androexp.cookbook

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_recipe_des.*

class RecipeDes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_des)

        val string = intent.getStringExtra("des")
        val byteArray = intent.getByteArrayExtra("img")

        setIntoViews(string, byteArray)

    }

    private fun setIntoViews(string: String?, byteArray: ByteArray?) {
        val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(byteArray, 0, it.size) }
        des_img.setImageBitmap(bitmap)
        des_rec.setText(string)

    }

}