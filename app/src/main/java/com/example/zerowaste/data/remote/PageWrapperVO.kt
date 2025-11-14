package com.example.zerowaste.data.remote

data class PageWrapperVO<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int
)
