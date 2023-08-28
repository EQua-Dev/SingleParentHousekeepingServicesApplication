package com.androidstrike.schoolprojects.singleparenthousekeepingservicesapplication.model

data class Service(
    val organisationProfileServiceID: String = "",
    val organisationID: String = "",
    val categoryOfServiceID: String = "",
    val typeOfServiceID: String = "",
    val organisationOfferedServiceName: String = "",
    val organisationOfferedServiceDetails: String = "",
    val organisationServiceFrequency: List<String> = listOf(),
    val organisationOfferedServicePrice: String = "",
    val serviceDiscountedPrice: String = "",
    val serviceAvailability: String = "",

    )
