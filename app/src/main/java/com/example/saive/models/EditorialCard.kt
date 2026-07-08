package com.example.saive.models

data class EditorialCard @JvmOverloads constructor(
    val title: String,
    val story: String,
    val material: String,
    val imageResId: Int,
    val ctaText: String = "EXPLORE PIECE"
)
