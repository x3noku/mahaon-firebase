package com.x3noku.mahaon.models

// there must be empty constructor
data class User(
    val uid: String? = null,
    val email: String? = null,
    val name: String? = null,
    val score: Int = 0
)