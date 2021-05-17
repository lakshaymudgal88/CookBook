package com.androexp.cookbook

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.androexp.cookbook.RecipeAdapter.VH
import com.parse.*
import kotlinx.android.synthetic.main.activity_post_recipe.*
import kotlinx.android.synthetic.main.reciepes_des.view.*

class RecipeAdapter(
    private val context: Context,
    private val recipeList: List<MainActivity.RecipeModel>,
    private val click: RecipeClick
) :
    RecyclerView.Adapter<VH>() {

    var isLike: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.reciepes_des, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val model = recipeList[position]
        holder.tvUserName.text = model.userName
        holder.tvRecName.text = model.recName
        holder.recipeImg.setImageBitmap(model.img)

        getFavStatus(model.recId, holder.favBtn)
        holder.itemView.setOnClickListener { view ->
            click.onRecipeClick(model.img, model.recDes)
        }

        holder.favBtn.setOnClickListener { view ->
            likeUnlike(model.recId, holder.favBtn, model.img)
        }

    }

    private fun likeUnlike(recId: String, favBtn: ImageView, bitmap: Bitmap) {

        val query = ParseQuery.getQuery<ParseObject>("Fav")
        query.whereEqualTo("rec_id", recId)
        query.whereEqualTo("user_name", ParseUser.getCurrentUser().username)
        query.getFirstInBackground { `object`, e ->
            if (e == null) {
                val objId = `object`.objectId
                query.getInBackground(objId) { `object`, e ->
                    if (e == null) {
                        `object`.deleteInBackground(DeleteCallback { e: ParseException? ->
                            if (e == null) {
                                Toast.makeText(context, "fav removed", Toast.LENGTH_SHORT).show()
                                getFavStatus(recId, favBtn)
                            } else {
                                Toast.makeText(
                                    context,
                                    "error in dlt: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                }
            } else {

                val obj = ParseObject("Fav")
                obj.put("user_name", ParseUser.getCurrentUser().username)
                obj.put("rec_id", recId)
                obj.saveInBackground { e: ParseException? ->
                    if (e == null) {
                        Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(context, "unLike ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getFavStatus(recId: String, favBtn: ImageView) {
        val user = ParseUser.getCurrentUser()
        if (user != null) {
            val query = ParseQuery.getQuery<ParseObject>("Fav")
            query.whereEqualTo("user_name", user.username)
            query.whereEqualTo("rec_id", recId)
            query.findInBackground(FindCallback { objects, e ->
                if (e == null) {
                    if (objects.size > 0) {
                        for (i in objects.indices) {
                            favBtn.setImageResource(R.drawable.ic_fav_after)
                        }
                    }
                } else {
                    favBtn.setImageResource(R.drawable.ic_fav_simple)
                }
            })
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvUserName: TextView = itemView.rec_user_name
        val recipeImg: ImageView = itemView.rec_img
        val tvRecName: TextView = itemView.rec_name
        val favBtn: ImageView = itemView.rec_fav
    }

    interface RecipeClick {
        fun onRecipeClick(img: Bitmap, des: String)
    }

}