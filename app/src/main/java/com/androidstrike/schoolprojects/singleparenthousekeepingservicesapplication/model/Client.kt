package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model

data class Client(
    val userId: String = "",
    val customerFirstName: String = "",
    val customerLastName: String = "",
    val customerEmail: String = "",
    val userAddress: String = "",
    //val userAddressLongitude: String = "",
    //val userAddressLatitude: String = "",
    val dateJoined: String = "",
    val role: String = "client",
    val customerMobileNumber: String = "",
    val wallet: String = ""

)
