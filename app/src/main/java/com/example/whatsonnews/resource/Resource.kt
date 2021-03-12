package com.example.whatsonnews.resource

sealed class Resource<T> ( // T is the generic response. Here it's NewsResponse
    val data: T? = null,
    val message: String? = null,
    val code: String? = null
){
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(data: T? = null, message: String? = null, code: String? = null) : Resource<T>(data, message, code)
    class Loading<T> : Resource<T>()
}