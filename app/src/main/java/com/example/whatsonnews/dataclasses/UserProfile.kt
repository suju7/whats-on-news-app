package com.example.whatsonnews.dataclasses

data class UserProfile(

    var fullName: String="",
    var username: String="",
    var email: String="",

    var countryCode: String="",

    var exclusive: Boolean=false,
    var health:Boolean=false,
    var business:Boolean=false,
    var entertainment:Boolean=false,
    var sports: Boolean=false,
    var technology: Boolean=false

)
