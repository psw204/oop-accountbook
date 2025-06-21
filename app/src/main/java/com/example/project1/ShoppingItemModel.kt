package com.example.project1

import com.google.firebase.database.Exclude

data class ShoppingItemModel(
    @get:Exclude
    var id: String = "",
    var itemName: String? = null,
    var isChecked: Boolean = false
) 