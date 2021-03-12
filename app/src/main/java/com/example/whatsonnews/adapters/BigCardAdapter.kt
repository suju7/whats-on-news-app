package com.example.whatsonnews.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsonnews.R

class BigCardAdapter(private val preferenceList: List<String>,
                     private val res: Resources) : RecyclerView.Adapter<BigCardAdapter.BigCardViewHolder>(){

    private val backgroundGradients = listOf(R.drawable.gradient_for_card_1, R.drawable.gradient_for_card_2, R.drawable.gradient_for_card_3,
            R.drawable.gradient_for_card_4, R.drawable.gradient_for_card_5)

    inner class BigCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bigCard: CardView = itemView.findViewById(R.id.bigCard)
        val bigCardText: TextView = itemView.findViewById(R.id.bigCardText)
        val constraintForCardBackground: ConstraintLayout = itemView.findViewById(R.id.bigCardConstraintLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigCardAdapter.BigCardViewHolder {
        return BigCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.each_big_card, parent, false))
    }

    override fun onBindViewHolder(holder: BigCardViewHolder, position: Int) {

        val currentCardText = preferenceList.elementAt(position)

        holder.apply {
            bigCardText.text = currentCardText
            constraintForCardBackground.background =
                ResourcesCompat.getDrawable(res, backgroundGradients[position % 5], null)
            bigCard.setOnClickListener {
                myCardClickListener?.let { it(currentCardText) }
                //onMyArticleClickListener?.let { it(article) }
            }
        }
    }

    override fun getItemCount(): Int {
        return preferenceList.size
    }

    private var myCardClickListener: ((title: String) -> Unit)? = null
    fun setOnMyCardClickListener(listener: (String) -> Unit) {
        myCardClickListener = listener
    }

}