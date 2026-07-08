package com.example.saive.adapters

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saive.R
import com.example.saive.models.EditorialCard
import com.example.saive.utils.ImageUtils
import com.google.android.material.button.MaterialButton

class EditorialCardAdapter(
    private val cards: List<EditorialCard>,
    private val onCardClick: (EditorialCard) -> Unit
) : RecyclerView.Adapter<EditorialCardAdapter.EditorialViewHolder>() {

    private var currentTopIndex = cards.size - 1

    class EditorialViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivEditorial: ImageView = view.findViewById(R.id.ivEditorial)
        val tvTitle: TextView = view.findViewById(R.id.tvCardTitle)
        val tvStory: TextView = view.findViewById(R.id.tvCardStory)
        val tvMaterial: TextView = view.findViewById(R.id.tvCardMaterial)
        val btnCTA: MaterialButton = view.findViewById(R.id.btnCTA)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_editorial_card, parent, false)
        return EditorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditorialViewHolder, position: Int) {
        val card = cards[position]
        
        holder.tvTitle.text = card.title
        holder.tvStory.text = card.story
        holder.tvMaterial.text = card.material
        ImageUtils.setSafeImage(holder.ivEditorial, card.imageResId)
        holder.btnCTA.text = card.ctaText

        holder.btnCTA.setOnClickListener {
            onCardClick(card)
        }
        
        holder.view.setOnClickListener {
            onCardClick(card)
        }
    }

    override fun getItemCount(): Int = cards.size
}
